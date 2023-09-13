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

package org.limbo.flowjob.agent.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.checker.TaskExecuteChecker;
import org.limbo.flowjob.agent.core.checker.TaskScheduleChecker;
import org.limbo.flowjob.agent.core.entity.Job;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.core.service.TaskService;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.api.param.agent.SubTaskCreateParam;
import org.limbo.flowjob.api.param.agent.TaskReportParam;
import org.limbo.flowjob.common.constants.AgentConstant;
import org.limbo.flowjob.common.constants.TaskConstant;
import org.limbo.flowjob.common.exception.RpcException;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.heartbeat.Heartbeat;
import org.limbo.flowjob.common.heartbeat.HeartbeatPacemaker;
import org.limbo.flowjob.common.rpc.EmbedRpcServer;
import org.limbo.flowjob.common.rpc.RpcServerStatus;
import org.limbo.flowjob.common.thread.CommonThreadPool;
import org.limbo.flowjob.common.thread.NamedThreadFactory;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
public class BaseScheduleAgent implements ScheduleAgent, Heartbeat {

    /**
     * 工作节点资源
     */
    @Getter
    private AgentResources resource;

    /**
     * 任务执行线程池
     */
    private ExecutorService threadPool;

    /**
     * 是否已经启动
     */
    private AtomicReference<RpcServerStatus> status;

    /**
     * 心跳起搏器
     */
    private HeartbeatPacemaker pacemaker;

    private TaskExecuteChecker taskExecuteChecker;

    private TaskScheduleChecker taskScheduleChecker;

    /**
     * 通信基础 URL
     */
    @Getter
    private URL url;

    /**
     * 任务上报线程池
     */
    private ScheduledExecutorService scheduledReportPool;

    /**
     * 远程调用
     */
    private AgentBrokerRpc brokerRpc;

    private EmbedRpcServer embedRpcServer;

    private TaskService taskService;

    private JobRepository jobRepository;

    public BaseScheduleAgent(URL url, AgentResources resource, AgentBrokerRpc brokerRpc, JobRepository jobRepository,
                             TaskService taskService, EmbedRpcServer embedRpcServer) {
        Objects.requireNonNull(url, "URL can't be null");
        Objects.requireNonNull(brokerRpc, "remote client can't be null");

        this.url = url;
        this.brokerRpc = brokerRpc;
        this.embedRpcServer = embedRpcServer;
        this.jobRepository = jobRepository;
        this.taskService = taskService;
        this.resource = resource;

        this.status = embedRpcServer.getStatus();
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public void start(Duration heartbeatPeriod) {
        Objects.requireNonNull(heartbeatPeriod);

        // 重复检测
        if (!status.compareAndSet(RpcServerStatus.IDLE, RpcServerStatus.INITIALIZING)) {
            return;
        }

        Heartbeat heartbeat = this;

        // 注册
        try {
            registerSelfToBroker();
        } catch (Exception e) {
            log.error("Register to broker has error", e);
            return;
        }

        // 启动RPC服务
        this.embedRpcServer.start();

        // 初始化线程池
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(resource.queueSize() <= 0 ? resource.concurrency() : resource.queueSize());
        threadPool = new ThreadPoolExecutor(
                resource.concurrency(), resource.concurrency(),
                5, TimeUnit.SECONDS, queue,
                NamedThreadFactory.newInstance("FlowJobAgentExecutor"),
                (r, e) -> {
                    throw new RejectedExecutionException();
                }
        );

        // job 状态上报
        scheduledReportPool = Executors.newScheduledThreadPool(resource.concurrency(), NamedThreadFactory.newInstance("FlowJobAgentJobReporter"));

        // 启动心跳
        if (pacemaker == null) {
            pacemaker = new HeartbeatPacemaker(heartbeat, Duration.ofSeconds(AgentConstant.HEARTBEAT_TIMEOUT_SECOND));
        }
        pacemaker.start();

        // task执行检测
        if (taskExecuteChecker == null) {
            taskExecuteChecker = new TaskExecuteChecker(taskService, Duration.ofSeconds(TaskConstant.TASK_REPORT_SECONDS + 5));
        }
        taskExecuteChecker.start();

        // tas下发检测
        if (taskScheduleChecker == null) {
            taskScheduleChecker = new TaskScheduleChecker(taskService, Duration.ofSeconds(5));
        }
        taskScheduleChecker.start();

        // 更新为运行中
        status.compareAndSet(RpcServerStatus.INITIALIZING, RpcServerStatus.RUNNING);
        log.info("schedule agent start!");
    }

    @Override
    public void sendHeartbeat() {
        try {
            brokerRpc.heartbeat(this);
        } catch (RpcException e) {
            log.warn("Agent send heartbeat failed");
            throw new IllegalStateException("Agent send heartbeat failed", e);
        }
    }

    /**
     * 向 Broker 注册当前 Worker
     */
    private void registerSelfToBroker() {
        try {
            // 调用 Broker 远程接口，并更新 Broker 信息
            brokerRpc.register(this);
        } catch (RegisterFailException e) {
            log.error("Worker register failed", e);
            throw e;
        }

        log.info("register success!");
    }

    @Override
    public void receiveJob(Job job) {
        assertRunning();

        int availableQueueSize = this.resource.availableQueueSize();
        if (jobRepository.count() >= availableQueueSize) {
            throw new IllegalArgumentException("Agent's queue is full, limit: " + availableQueueSize);
        }

        if (jobRepository.save(job)) {
            try {
                job.setTaskDispatcher(taskService.getTaskDispatcher());
                job.setTaskRepository(taskService.getTaskRepository());
                job.setJobRepository(jobRepository);
                job.setBrokerRpc(brokerRpc);
                job.setScheduledReportPool(scheduledReportPool);

                this.threadPool.submit(job);
            } catch (RejectedExecutionException e) {
                jobRepository.delete(job.getId());
                // 拒绝时候的处理
                throw new IllegalStateException("Schedule job failed, maybe thread exhausted. job=" + job.getId());
            }
        }
        // 可能重复下发到同个节点，这种时候不需要处理
    }

    @Override
    public void receiveSubTasks(SubTaskCreateParam param) {
        assertRunning();

        String jobId = param.getJobId();
        List<SubTaskCreateParam.SubTaskInfoParam> subTaskParams = param.getSubTasks();
        if (CollectionUtils.isEmpty(subTaskParams)) {
            throw new IllegalArgumentException("subTasks is empty");
        }

        Job job = jobRepository.getById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("job not found jobId:" + jobId);
        }
        if (JobType.MAP != job.getType() && JobType.MAP_REDUCE != job.getType()) {
            throw new IllegalArgumentException("Job Type doesn't match jobId:" + jobId + " type:" + job.getType());
        }

        // 防止重复id
        subTaskParams = subTaskParams.stream().filter(p -> StringUtils.isNotBlank(p.getTaskId())).collect(Collectors.toList());
        List<String> subTaskIds = subTaskParams.stream().map(SubTaskCreateParam.SubTaskInfoParam::getTaskId).collect(Collectors.toList());
        Set<String> existTaskIds = taskService.getExistTaskIds(jobId, subTaskIds);
        subTaskParams = subTaskParams.stream().filter(p -> !existTaskIds.contains(p.getTaskId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subTaskParams)) {
            if (log.isDebugEnabled()) {
                log.debug("subTasks is empty param={}", param);
            }
            return;
        }

        List<Task> tasks = new ArrayList<>();
        for (SubTaskCreateParam.SubTaskInfoParam subTaskInfoParam : subTaskParams) {
            Task newTask = TaskFactory.createTask(subTaskInfoParam.getTaskId(), job, subTaskInfoParam.getData(), TaskType.MAP, null);
            tasks.add(newTask);
        }

        if (!taskService.batchSave(tasks)) {
            throw new IllegalArgumentException("batch save task fail jobId:" + jobId);
        }

        for (Task task : tasks) {
            CommonThreadPool.IO.submit(() -> taskService.getTaskDispatcher().dispatch(task));
        }
    }

    @Override
    public boolean reportTaskExecuting(TaskReportParam param) {
        assertRunning();

        if (param == null || param.getWorkerId() == null) {
            return false;
        }

        String url = param.getWorkerAddress() == null ? "" : param.getWorkerAddress().toString();

        return taskService.getTaskRepository().executing(param.getJobId(), param.getTaskId(), param.getWorkerId(), url);
    }

    @Override
    public void reportTask(TaskReportParam param) {
        assertRunning();

        taskService.getTaskRepository().report(param.getJobId(), param.getTaskId());
    }

    @Override
    public void taskSuccess(String jobId, String taskId, Attributes context, String result) {
        assertRunning();

        Task task = taskService.getTaskRepository().getById(jobId, taskId);
        if (task == null) {
            throw new IllegalArgumentException("task not found jobId:" + jobId + " taskId:" + taskId);
        }

        taskService.taskSuccess(task, context, result);
    }

    @Override
    public void taskFail(String jobId, String taskId, String errorMsg, String errorStackTrace) {
        assertRunning();

        Task task = taskService.getTaskRepository().getById(jobId, taskId);
        if (task == null) {
            throw new IllegalArgumentException("task not found jobId:" + jobId + " taskId:" + taskId);
        }

        taskService.taskFail(task, errorMsg, errorStackTrace);
    }

    /**
     * 验证 worker 正在运行中
     */
    private void assertRunning() {
        if (this.status.get() != RpcServerStatus.RUNNING) {
            throw new IllegalStateException("Agent is not running: " + this.status.get());
        }
    }

    @Override
    public void stop() {
        this.pacemaker.stop();
        this.taskExecuteChecker.stop();
        this.embedRpcServer.stop();
    }

    @Override
    public void beat() {
        this.sendHeartbeat();
    }

}
