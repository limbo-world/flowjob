/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.agent.core.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.TaskFactory;
import org.limbo.flowjob.agent.core.Worker;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.constants.JobConstant;
import org.limbo.flowjob.common.exception.JobException;
import org.limbo.flowjob.common.thread.NamedThreadFactory;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Job生命周期管理
 *
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
@Getter
@Setter(AccessLevel.NONE)
@Builder
public class Job implements Runnable {

    /**
     * job d
     */
    private String id;

    /**
     * 实例
     */
    private String instanceId;

    /**
     * 类型
     *
     * @see JobType
     */
    private JobType type;

    /**
     * 执行器的名称
     */
    private String executorName;

    /**
     * 负载策略
     */
    private LoadBalanceType loadBalanceType;

    /**
     * 上下文元数据
     */
    private Attributes context;

    /**
     * job配置的属性
     */
    private Attributes attributes;

    private ScheduledExecutorService scheduledReportPool;

    private ScheduledFuture<?> reportScheduledFuture;

    private ScheduledFuture<?> completeScheduledFuture;

    private TaskCounter taskCounter;

    // =======  注入 ========
    private TaskDispatcher taskDispatcher;

    private TaskRepository taskRepository;

    private JobRepository jobRepository;

    private AgentBrokerRpc brokerRpc;

    @Override
    public void run() {
        start();
    }

    public void start() {
        try {
            // 反馈执行中 -- 排除由于网络问题导致的失败可能性
            boolean success = reportJobExecuting(id, 3);
            if (!success) {
                jobRepository.delete(id);
                return; // 可能已经下发给其它节点
            }
            this.scheduledReportPool = Executors.newScheduledThreadPool(2, NamedThreadFactory.newInstance("FlowJobJobTrackerReportPool"));
            // 开启任务上报
            reportScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new StatusReportRunnable(), 1, JobConstant.JOB_REPORT_SECONDS, TimeUnit.SECONDS);
            // 计数
            taskCounter = new TaskCounter();
            // 执行
            schedule();
        } catch (Exception e) {
            log.error("Failed to receive job id={}", id, e);
            jobRepository.delete(id);
            taskRepository.deleteByJobId(id);
            throw new JobException(id, e.getMessage(), e);
        }
    }

    public boolean saveTask(Collection<Task> tasks) {
        boolean saved = taskRepository.batchSave(tasks);
        if (saved) {
            taskCounter.total.addAndGet(tasks.size());
            taskCounter.scheduling.addAndGet(tasks.size());
        }
        return saved;
    }

    public void stop() {
        if (reportScheduledFuture != null) {
            reportScheduledFuture.cancel(true);
        }
        if (completeScheduledFuture != null) {
            completeScheduledFuture.cancel(true);
        }
        if (scheduledReportPool != null) {
            scheduledReportPool.shutdown();
        }
    }

    private void schedule() {
        log.info("start schedule job={}", this);

        // 根据job类型创建task
        List<Task> tasks = createRootTasks();

        // 如果可以创建的任务为空（只有广播任务）
        if (CollectionUtils.isEmpty(tasks)) {
            handleSuccess();
            return;
        }

        try {
            if (saveTask(tasks)) {
                for (Task task : tasks) {
                    taskDispatcher.dispatch(task);
                }
            }
        } catch (Exception e) {
            log.error("batchSave tasks({}) failed.", tasks, e);
        }
    }

    public boolean taskExecuting(String taskId, String workerId, URL workerAddress) {
        String url = workerAddress == null ? "" : workerAddress.toString();
        boolean updated = taskRepository.executing(id, taskId, workerId, url);
        if (updated) {
            taskCounter.executing.incrementAndGet();
        }
        return updated;
    }

    /**
     * task 成功处理
     */
    public void taskSuccess(Task task, Attributes context, String result) {
        task.success(context, result);
        boolean updated = taskRepository.success(task);
        if (!updated) { // 已经被更新 无需重复处理
            return;
        }

        taskCounter.succeed.incrementAndGet();

        switch (task.getType()) {
            case STANDALONE:
            case REDUCE:
            case BROADCAST:
                handleSuccess();
                break;
            case SHARDING:
                break;
            case MAP:
                dealMapTaskSuccess(task);
                break;
            default:
                log.warn("can' find task type id:{} type:{}", task.getId(), task.getType());
                break;
        }

    }

    /**
     * 检测是否所有task都已经完成
     * 如果已经完成 下发 ReduceTask
     */
    private void dealMapTaskSuccess(Task task) {
        if (taskCounter.total.get() > taskCounter.succeed.get()) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }

        List<String> results = taskRepository.getAllTaskResult(task.getJobId(), TaskType.MAP);
        List<Map<String, Object>> mapResults = results.stream()
                .map(r -> JacksonUtils.parseObject(r, new TypeReference<Map<String, Object>>() {
                }))
                .collect(Collectors.toList());
        Task reduceTask = TaskFactory.create(TaskType.REDUCE.name(), this, mapResults, TaskType.REDUCE, null);
        saveTask(Collections.singletonList(reduceTask));
        taskDispatcher.dispatch(reduceTask);
    }

    /**
     * 通知/更新job状态
     */
    public synchronized void handleSuccess() {
        if (taskCounter.total.get() == taskCounter.succeed.get()) {
            // 开启任务执行完成反馈
            completeScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new CompleteReportRunnable(this, true, null), 0, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * task失败处理
     */
    public void taskFail(Task task, String errorMsg, String errorStackTrace) {
        if (StringUtils.isBlank(errorMsg)) {
            errorMsg = "";
        }
        if (StringUtils.isBlank(errorStackTrace)) {
            errorStackTrace = "";
        }
        task.fail(errorMsg, errorStackTrace);
        boolean updated = taskRepository.fail(task);
        if (!updated) { // 已经被更新 无需重复处理
            return;
        }
        taskCounter.failed.incrementAndGet();

        // 判断是否为最后一个task
        handleFail(errorMsg);
        // 终止其它执行中的task
    }

    /**
     * 通知/更新job状态
     */
    public synchronized void handleFail(String errorMsg) {
        if (taskCounter.succeed.get() + taskCounter.failed.get() == taskCounter.total.get()) {
            // 开启任务执行完成反馈
            completeScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new CompleteReportRunnable(this, false, errorMsg), 0, 1, TimeUnit.SECONDS);
        }
    }

    private boolean reportJobExecuting(String id, int retryTimes) {
        while (retryTimes > 0) {
            try {
                return brokerRpc.reportExecuting(id);
            } catch (Exception e) {
                log.error("reportJobExecuting fail job={} times={}", id, retryTimes, e);
                retryTimes--;
            }
        }
        return false;
    }

    public List<Task> createRootTasks() {
        Job job = this;
        List<Task> tasks = new ArrayList<>();
        switch (job.getType()) {
            case STANDALONE:
                tasks.add(TaskFactory.create(job.getType().name(), job, null, TaskType.STANDALONE, null));
                break;
            case BROADCAST:
                List<Worker> workers = brokerRpc.availableWorkers(job.getId(), true, true, false, false);
                int idx = 1;
                for (Worker worker : workers) {
                    Task task = TaskFactory.create(String.valueOf(idx), job, null, TaskType.BROADCAST, worker);
                    tasks.add(task);
                    idx++;
                }
                break;
            case MAP:
            case MAP_REDUCE:
                tasks.add(TaskFactory.create(TaskType.SHARDING.name(), job, null, TaskType.SHARDING, null));
                break;
            default:
                throw new JobException(job.getId(), MsgConstants.UNKNOWN + " job type:" + job.getType().type);
        }

        return tasks;
    }

    private static class TaskCounter {

        AtomicInteger total = new AtomicInteger(0);

        AtomicInteger scheduling = new AtomicInteger(0);

        AtomicInteger executing = new AtomicInteger(0);

        AtomicInteger succeed = new AtomicInteger(0);

        AtomicInteger failed = new AtomicInteger(0);

    }

    private class StatusReportRunnable implements Runnable {

        @Override
        public void run() {
            brokerRpc.reportJob(id);
        }
    }

    private class CompleteReportRunnable implements Runnable {

        private final Job job;

        private final boolean success;

        private final String errorMsg;

        public CompleteReportRunnable(Job job, boolean success, String errorMsg) {
            this.job = job;
            this.success = success;
            this.errorMsg = errorMsg;
        }

        @Override
        public void run() {
            try {
                if (success) {
                    if (brokerRpc.feedbackJobSucceed(job)) {
                        jobRepository.delete(job.getId());
                    }
                } else {
                    if (brokerRpc.feedbackJobFail(job, errorMsg)) {
                        jobRepository.delete(job.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Job Complete Fail job={} success={} errorMsg={}", job, success, errorMsg, e);
            }
        }
    }

}
