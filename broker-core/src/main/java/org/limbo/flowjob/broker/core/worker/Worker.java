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
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpc;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpcFactory;
import org.limbo.flowjob.common.constants.WorkerStatus;
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
    private String id;

    /**
     * Worker 名称
     */
    private String name;

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
    @Setter
    private List<WorkerExecutor> executors;

    /**
     * Worker 标签
     */
    private Map<String, List<String>> tags;

    /**
     * Worker 状态指标
     */
    @Setter
    private WorkerMetric metric;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getServerId() {
        return id;
    }

    /**
     * 当前 worker 是否处在存活状态。熔断状态不算存活状态
     */
    @Override
    public boolean isAlive() {
        return WorkerStatus.RUNNING == status;
    }

    /**
     * 获取 worker RPC 通信时的接口访问地址
     */
    @Override
    public URL getUrl() {
        return rpcBaseUrl;
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


    /**
     * 是否需要记录 worker 统计信息
     * @param metric 统计数据
     */
    public void heartbeat(WorkerMetric metric) {
        this.metric = metric;
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
