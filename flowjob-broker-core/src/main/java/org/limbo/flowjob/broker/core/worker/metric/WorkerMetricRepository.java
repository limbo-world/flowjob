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

package org.limbo.flowjob.broker.core.worker.metric;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface WorkerMetricRepository {

    /**
     * 更新worker指标信息
     * @param workerId workerId
     * @param metric worker指标信息
     */
    void updateMetric(String workerId, WorkerMetric metric);

    /**
     * 根据worker节点ID查询节点的指标信息
     * @param workerId workerId
     * @return worker节点的指标信息
     */
    WorkerMetric getMetric(String workerId);

}
