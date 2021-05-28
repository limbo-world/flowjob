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

package org.limbo.flowjob.tracker.core.tracker.worker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.limbo.flowjob.tracker.core.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatisticsRepository;
import reactor.core.publisher.Mono;

/**
 * 在Tracker端，作业执行节点的抽象。
 *
 * @author Brozen
 * @since 2021-05-14
 */
@AllArgsConstructor
public abstract class Worker implements WorkerDefinition {

    /**
     * worker节点ID
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String id;

    /**
     * worker服务的通信协议
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private WorkerProtocol protocol;

    /**
     * worker服务的通信IP
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String ip;

    /**
     * worker服务的通信端口
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Integer port;

    /**
     * worker节点状态
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private WorkerStatus status;

    /**
     * 本worker节点最近一次上报的指标信息
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private WorkerMetric metric;

    /**
     * 用户更新worker
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private WorkerRepository repository;

    /**
     * worker统计信息repo
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private WorkerStatisticsRepository statisticsRepository;

    /**
     * 更新worker
     */
    protected void updateWorker() {
        repository.updateWorker(this);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public WorkerStatistics getStatistics() {
        return statisticsRepository.getWorkerStatistics(getId());
    }

    /**
     * worker节点心跳检测。
     * @return 返回worker节点的指标信息。
     */
    public abstract Mono<WorkerMetric> ping();

    /**
     * 发送一个作业到worker执行。当worker接受此job后，将触发返回的{@link Mono}
     * @param context 作业执行上下文
     * @return worker接受job后触发
     */
    public abstract Mono<SendJobResult> sendJobContext(JobContext context) throws JobWorkerException;

    /**
     * 解注册此worker，worker的状态将被标记为{@link WorkerStatus#TERMINATED}
     */
    public abstract void unregister();

}
