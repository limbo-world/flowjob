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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
@Data
public class Job implements Runnable {

    /**
     * 实例id
     */
    private String id;

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


    // =======  注入 ========
    private TaskDispatcher taskDispatcher;

    private TaskRepository taskRepository;

    private JobRepository jobRepository;

    private AgentBrokerRpc brokerRpc;

    private ScheduledExecutorService scheduledReportPool;

    private ScheduledFuture<?> reportScheduledFuture = null;

    private ScheduledFuture<?> completeScheduledFuture = null;

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
            // 开启任务上报
            reportScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new StatusReportRunnable(), 1, JobConstant.JOB_REPORT_SECONDS, TimeUnit.SECONDS);
            // 执行
            schedule();
        } catch (Exception e) {
            log.error("Failed to receive job id={}", id, e);
            jobRepository.delete(id);
            taskRepository.deleteByJobId(id);
            throw new JobException(id, e.getMessage(), e);
        }
    }

    public void stop() {
        if (reportScheduledFuture != null) {
            reportScheduledFuture.cancel(true);
        }
        if (completeScheduledFuture != null) {
            completeScheduledFuture.cancel(true);
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
            if (taskRepository.batchSave(tasks)) {
                for (Task task : tasks) {
                    taskDispatcher.dispatch(task);
                }
            }
        } catch (Exception e) {
            log.error("batchSave tasks({}) failed.", tasks, e);
        }
    }

    /**
     * 通知/更新job状态
     */
    public void handleSuccess() {
        // 开启任务执行完成反馈
        completeScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new CompleteReportRunnable(this, true, null), 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 通知/更新job状态
     */
    public void handleFail(String errorMsg) {
        // 开启任务执行完成反馈
        completeScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new CompleteReportRunnable(this, false, errorMsg), 0, 1, TimeUnit.SECONDS);
    }

    private boolean reportJobExecuting(String id, int retryTimes) {
        if (retryTimes < 0) {
            return false;
        }
        try {
            return brokerRpc.reportExecuting(id);
        } catch (Exception e) {
            log.error("reportJobExecuting fail job={} times={}", id, retryTimes, e);
            retryTimes--;
            return reportJobExecuting(id, retryTimes);
        }
    }

    public List<Task> createRootTasks() {
        Job job = this;
        List<Task> tasks = new ArrayList<>();
        switch (job.getType()) {
            case STANDALONE:
                tasks.add(TaskFactory.createTask(job.getType().name(), job, null, TaskType.STANDALONE, null));
                break;
            case BROADCAST:
                List<Worker> workers = brokerRpc.availableWorkers(job.getId(), true, true, false, false);
                int idx = 1;
                for (Worker worker : workers) {
                    Task task = TaskFactory.createTask(String.valueOf(idx), job, null, TaskType.BROADCAST, worker);
                    tasks.add(task);
                    idx++;
                }
                break;
            case MAP:
            case MAP_REDUCE:
                tasks.add(TaskFactory.createTask(TaskType.SHARDING.name(), job, null, TaskType.SHARDING, null));
                break;
            default:
                throw new JobException(job.getId(), MsgConstants.UNKNOWN + " job type:" + job.getType().type);
        }

        return tasks;
    }

    private class StatusReportRunnable implements Runnable {

        @Override
        public void run() {
            brokerRpc.reportJob(id);
        }
    }

    private class CompleteReportRunnable implements Runnable {

        private Job job;

        private boolean success;

        private String errorMsg;

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
