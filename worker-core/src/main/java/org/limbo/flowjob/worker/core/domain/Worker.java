/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.api.clent.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerResourceParam;
import org.limbo.flowjob.common.utils.UUIDUtils;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.executor.TaskExecutor;
import org.limbo.flowjob.worker.core.executor.TaskExecutorContext;
import org.limbo.flowjob.worker.core.executor.TaskRepository;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;
import org.limbo.flowjob.worker.core.rpc.exceptions.BrokerRpcException;
import org.limbo.flowjob.worker.core.rpc.exceptions.RegisterFailException;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 工作节点实例
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class Worker {

    @Getter
    private final String id;

    /**
     * Worker 通信基础 URL
     */
    private URL workerRpcBaseURL;

    /**
     * 工作节点资源
     */
    private WorkerResource resource;

    /**
     * 执行器名称 - 执行器 映射关系
     */
    private final Map<String, TaskExecutor> executors;

    /**
     * job 管理中心
     */
    private final TaskRepository taskRepository;

    /**
     * 远程调用
     */
    private BrokerRpc brokerRpc;

    /**
     * 是否已经启动
     */
    private AtomicReference<WorkerStatus> status;

    /**
     * 心跳起搏器
     */
    private WorkerHeartbeat pacemaker;

    public Worker(String id, int queueSize, List<TaskExecutor> executors, BrokerRpc brokerRpc) {
        Verifies.notEmpty(executors, "empty executors");
        Verifies.notNull(brokerRpc, "remote client can't be null");

        this.id = StringUtils.isBlank(id) ? UUIDUtils.randomID() : id;
        this.taskRepository = new TaskRepository();
        this.resource = WorkerResource.create(queueSize, this.taskRepository);
        this.brokerRpc = brokerRpc;

        this.executors = new ConcurrentHashMap<>();
        for (TaskExecutor executor : executors) {
            Verifies.notBlank(executor.getName(), "has blank executor name");
            this.executors.put(executor.getName(), executor);
        }

        this.status = new AtomicReference<>(WorkerStatus.IDLE);
    }


    /**
     * 启动当前 Worker
     *
     * @param baseURL Worker 启动的 RPC 服务的 baseUrl
     * @param heartbeatPeriod 心跳间隔
     */
    public void start(URL baseURL, Duration heartbeatPeriod) {
        Objects.requireNonNull(baseURL);
        Objects.requireNonNull(heartbeatPeriod);

        // 重复检测
        if (!this.status.compareAndSet(WorkerStatus.IDLE, WorkerStatus.RUNNING)) {
            return;
        }

        this.workerRpcBaseURL = baseURL;
        if (this.pacemaker == null) {
            this.pacemaker = new WorkerHeartbeat(this, Duration.ofSeconds(1));
        }

        // 注册
        registerSelfToBroker();

        // 启动心跳
        this.pacemaker.start();
    }

    /**
     * 向 Broker 注册当前 Worker
     */
    protected void registerSelfToBroker() {

        // 执行器
        List<WorkerExecutorRegisterParam> executors = this.executors.values().stream()
                .map(executor -> {
                    WorkerExecutorRegisterParam executorRegisterParam = new WorkerExecutorRegisterParam();
                    executorRegisterParam.setName(executor.getName());
                    executorRegisterParam.setDescription(executor.getDescription());
                    executorRegisterParam.setType(executor.getType());
                    return executorRegisterParam;
                })
                .collect(Collectors.toList());

        // 可用资源
        WorkerResourceParam resourceParam = new WorkerResourceParam();
        resourceParam.setAvailableCpu(resource.getAvailableCpu());
        resourceParam.setAvailableRAM(resource.getAvailableRam());
        resourceParam.setAvailableQueueLimit(resource.getAvailableQueueSize());

        // 组装注册参数
        WorkerRegisterParam registerParam = new WorkerRegisterParam();
        registerParam.setId(this.id);
        registerParam.setProtocol(this.workerRpcBaseURL.getProtocol());
        registerParam.setHost(this.workerRpcBaseURL.getHost());
        registerParam.setPort(this.workerRpcBaseURL.getPort());
        registerParam.setExecutors(executors);
        registerParam.setAvailableResource(resourceParam);

        try {
            // 调用 Broker 远程接口，并更新 Broker 信息
            brokerRpc.register(registerParam);
        } catch (RegisterFailException e) {
            log.error("Worker register failed, param={}, response={}",
                    JacksonUtils.toJSONString(registerParam), e);
            throw new IllegalStateException("Worker register failed");
        }

        log.info("register success !");
    }


    /**
     * Just beat it
     * 发送心跳
     */
    protected void sendHeartbeat() {
        // 可用资源
        WorkerResourceParam resource = new WorkerResourceParam();
        resource.setAvailableCpu(this.resource.getAvailableCpu());
        resource.setAvailableRAM(this.resource.getAvailableRam());
        resource.setAvailableQueueLimit(this.resource.getAvailableQueueSize());

        // 组装心跳参数
        WorkerHeartbeatParam heartbeatParam = new WorkerHeartbeatParam();
        heartbeatParam.setWorkerId(id);
        heartbeatParam.setAvailableResource(resource);

        try {
            brokerRpc.heartbeat(heartbeatParam);
        } catch (BrokerRpcException e) {
            log.warn("Worker send heartbeat failed");
            throw new IllegalStateException("Worker send heartbeat failed", e);
        }
    }


    /**
     * 接收 Broker 发送来的任务
     * @param task 任务数据
     */
    public synchronized void receiveTask(Task task) {
        // 找到执行器，校验是否存在
        TaskExecutor executor = executors.get(task.getExecutorName());
        Verifies.notNull(executor, "Unsupported executor: " + task.getExecutorName());

        Verifies.verify(
                this.taskRepository.count() < this.resource.getAvailableQueueSize(),
                "Worker's queue is full, limit: " + this.resource.getAvailableQueueSize()
        );

        // todo 检测资源余量是否充足：cpu/ram/queue

        TaskExecutorContext context = new TaskExecutorContext(taskRepository, executor, brokerRpc, task);
        context.start();
    }


}
