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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.exceptions.TaskReceiveException;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatisticsRepository;

import java.util.List;

/**
 * 在Tracker端，作业执行节点的抽象。
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public abstract class Worker {

    /**
     * worker服务使用的通信协议，默认为Http协议。
     */
    private WorkerProtocol protocol;

    /**
     * worker服务的通信host
     */
    private String host;

    /**
     * worker服务的通信端口
     */
    private Integer port;

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

    public Worker(WorkerRepository workerRepository,
                  WorkerMetricRepository metricRepository,
                  WorkerStatisticsRepository statisticsRepository) {
        this.workerRepository = workerRepository;
        this.metricRepository = metricRepository;
        this.statisticsRepository = statisticsRepository;
    }


    /**
     * 更新worker
     */
    protected void updateWorker() {
        workerRepository.updateWorker(this);
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

    public abstract String getWorkerId();

    /**
     * worker节点心跳检测。
     * @return 返回worker节点的指标信息。
     */
    public abstract WorkerMetric ping();

    /**
     * 发送一个作业到worker执行。当worker接受此task后，将触发返回
     * @param instance 作业实例
     * @return worker接受task后触发
     */
    public abstract TaskReceiveDTO sendTask(Task instance) throws TaskReceiveException;

    /**
     * 解注册此worker，worker的状态将被标记为{@link WorkerStatus#TERMINATED}
     */
    public abstract void unregister();

}
