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

package org.limbo.flowjob.tracker.commons.beans.worker.domain;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.beans.worker.valueobject.JobDescription;
import org.limbo.flowjob.tracker.commons.beans.worker.valueobject.WorkerAvailableResource;

import java.util.List;

/**
 * worker的指标信息。
 *
 * @author Brozen
 * @since 2021-05-17
 */
@Data
public class WorkerMetric {

    /**
     * worker节点ID
     */
    private String workerId;

    /**
     * worker节点上正在执行中的作业，瞬时态数据，可能并发不安全
     */
    private List<JobDescription> executingJobs;

    /**
     * worker可用的资源
     */
    private WorkerAvailableResource availableResource;

    /**
     * 指标上报时间戳
     */
    private Long timestamp;

}
