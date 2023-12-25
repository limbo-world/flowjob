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
import org.limbo.flowjob.api.constants.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
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
public class Worker implements LBServer {

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
    private URL url;

    /**
     * 与broker通信 URL
     */
    private URL brokerUrl;

    /**
     * worker节点状态
     */
    private WorkerStatus status;

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

    /**
     * 是否启用 不启用则无法下发任务
     */
    private boolean enabled;

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

    @Override
    public URL getUrl() {
        return url;
    }


    /**
     * 更新 worker 的注册信息
     */
    public void register(URL rpcBaseUrl, URL brokerUrl, Map<String, List<String>> tags,
                         List<WorkerExecutor> executors, WorkerMetric metric) {
        this.url = Objects.requireNonNull(rpcBaseUrl, "rpcBaseUrl");
        this.brokerUrl = Objects.requireNonNull(brokerUrl, "brokerUrl");
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

}
