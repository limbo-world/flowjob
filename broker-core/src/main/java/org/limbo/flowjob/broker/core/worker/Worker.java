/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.worker;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpc;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpcFactory;
import org.limbo.flowjob.common.lb.LBServer;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 在Tracker端，作业执行节点的抽象。
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter(AccessLevel.NONE)
@ToString
@Builder(builderClassName = "Builder")
public class Worker implements WorkerRpc, LBServer {

    /**
     * Worker ID
     */
    private String workerId;

    /**
     * worker 通信的基础 URL
     */
    private URL rpcBaseUrl;

    /**
     * worker节点状态
     */
    private WorkerStatus status;

    /**
     * 是否启用 不启用则无法下发任务
     */
    private Boolean isEnabled;

    /**
     * Worker 的 RPC 通信协议
     */
    private volatile WorkerRpc rpc;

    /**
     * Worker 执行器
     */
    private List<WorkerExecutor> executors;

    /**
     * Worker 标签
     */
    private Map<String, List<String>> tags;

    /**
     * Worker 状态指标
     */
    private WorkerMetric metric;


    @Override
    public String getServerId() {
        return workerId;
    }

    /**
     * 当前 worker 是否处在存活状态。熔断状态不算存活状态
     */
    @Override
    public boolean isAlive() {
        return WorkerStatus.RUNNING == status;
    }


    /**
     * 更新 worker 的注册信息
     */
    public void register(URL rpcBaseUrl, Map<String, List<String>> tags,
                         List<WorkerExecutor> executors, WorkerMetric metric) {
        this.rpcBaseUrl = Objects.requireNonNull(rpcBaseUrl, "rpcBaseUrl");
        setTags(tags);
        setExecutors(executors);
        setMetric(metric);

        this.status = WorkerStatus.RUNNING;
    }


    public void heartbeat(WorkerMetric metric) {

    }


    /**
     * 更新 worker tag 信息
     */
    protected void setTags(Map<String, List<String>> tags) {
        // 复制数据，防止入参变化影响 Worker 的 tags 数据（防止副作用）
        HashMap<String, List<String>> tempTags = new HashMap<>();
        tags.forEach((k, v) -> tempTags.put(k, new ArrayList<>(v)));
        this.tags = tempTags;
    }


    /**
     * 更新worker节点指标信息
     * @param metric worker节点指标
     */
    protected void setMetric(WorkerMetric metric) {
        if (!this.workerId.equals(metric.getWorkerId())) {
            throw new IllegalArgumentException("worker id mismatch");
        }

        this.metric = metric;
    }


    /**
     * 更新 worker 的执行器信息
     * @param executors 新的执行器数据
     */
    protected void setExecutors(List<WorkerExecutor> executors) {
        boolean anyExecutorMismatch = executors.stream()
                .anyMatch(e -> !this.workerId.equals(e.getWorkerId()));
        if (anyExecutorMismatch) {
            throw new IllegalArgumentException("worker id mismatch");
        }

        this.executors = executors;
    }


    /**
     * 懒加载 Worker RPC 模块
     */
    @Delegate(types = WorkerRpc.class)
    private synchronized WorkerRpc getRPC() {
        if (this.rpc == null) {
            WorkerRpcFactory factory = WorkerRpcFactory.getInstance();
            this.rpc = factory.createRPC(this);
        }

        return this.rpc;
    }

}
