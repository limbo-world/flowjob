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
import org.limbo.flowjob.broker.core.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpc;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpcFactory;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatisticsRepository;

import java.net.URL;
import java.util.List;

/**
 * 在Tracker端，作业执行节点的抽象。
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@Builder(builderClassName = "Builder")
public class Worker implements WorkerRpc {

    /**
     * Worker ID
     */
    private String workerId;

    /**
     * worker 通信的基础 URL
     */
    private URL rpcBaseUrl;

    /**
     * 执行器
     */
    private List<WorkerExecutor> executors;

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

    // ------------------------ 分隔
    /**
     * 用户更新worker
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private WorkerRepository workerRepository;

    /**
     * worker节点指标信息repo
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private WorkerMetricRepository metricRepository;

    /**
     * worker统计信息repo
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private WorkerStatisticsRepository statisticsRepository;


    /**
     * 当前 worker 是否处在存活状态。熔断状态不算存活状态
     */
    public boolean isAlive() {
        return this.status == WorkerStatus.RUNNING;
    }


    /**
     * 更新 worker 的注册信息
     * @return 自身，链式调用
     */
    public Worker updateRegisterInfo(URL rpcBaseUrl) {
        this.rpcBaseUrl = rpcBaseUrl;
        this.status = WorkerStatus.RUNNING;
        return this;
    }


    /**
     * 获取本worker节点最近一次上报的指标信息
     * @return worker节点指标信息
     */
    public WorkerMetric getMetric() {
        return metricRepository.getMetric(getWorkerId());
    }


    /**
     * 更新worker节点指标信息
     * @param metric worker节点指标
     */
    public void updateMetric(WorkerMetric metric) {
        metric.setWorkerId(getWorkerId());
        metricRepository.updateMetric(metric);
    }


    /**
     * 获取此worker对应的统计记录
     * @return 此worker对应的统计记录
     */
    public WorkerStatistics getStatistics() {
        return statisticsRepository.getWorkerStatistics(getWorkerId());
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
