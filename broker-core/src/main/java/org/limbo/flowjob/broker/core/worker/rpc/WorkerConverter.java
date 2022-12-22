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

package org.limbo.flowjob.broker.core.worker.rpc;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.dto.WorkerAvailableResourceDTO;
import org.limbo.flowjob.api.dto.WorkerMetricDTO;
import org.limbo.flowjob.api.param.TaskSubmitParam;
import org.limbo.flowjob.broker.core.domain.task.MapTask;
import org.limbo.flowjob.broker.core.domain.task.ReduceTask;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/10/21
 */
public class WorkerConverter {

    public static WorkerMetric toDO(WorkerMetricDTO dto) {
        return new WorkerMetric(dto.getExecutingJobs(), toDO(dto.getAvailableResource()));
    }

    public static WorkerAvailableResource toDO(WorkerAvailableResourceDTO dto) {
        return new WorkerAvailableResource(dto.getAvailableCpu(), dto.getAvailableRam(), dto.getAvailableQueueLimit());
    }

    public static TaskSubmitParam toTaskSubmitParam(Task task) {
        TaskSubmitParam taskSubmitParam = new TaskSubmitParam();
        taskSubmitParam.setTaskId(task.getMetaId());
        taskSubmitParam.setPlanId(task.getPlanId());
        taskSubmitParam.setJobId(task.getJobId());
        taskSubmitParam.setJobInstanceId(task.getJobInstanceId());
        taskSubmitParam.setExecutorName(task.getExecutorName());
        taskSubmitParam.setContext(task.getContext() == null ? Collections.emptyMap() : task.getContext().toMap());
        taskSubmitParam.setAttributes(task.getAttributes() == null ? Collections.emptyMap() : task.getAttributes().toMap());

        if (task instanceof MapTask) {
            MapTask mapTask = (MapTask) task;
            taskSubmitParam.setMapAttributes(mapTask.getMapAttributes() == null ? Collections.emptyMap() : mapTask.getMapAttributes().toMap());
        } else if (task instanceof ReduceTask) {
            ReduceTask reduceTask = (ReduceTask) task;
            List<Map<String, Object>> reduceAttrs = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(reduceTask.getReduceAttributes())) {
                reduceAttrs = reduceTask.getReduceAttributes().stream()
                        .filter(attrs -> attrs != null && !attrs.isEmpty())
                        .map(Attributes::toMap)
                        .collect(Collectors.toList());
            }
            taskSubmitParam.setReduceAttributes(reduceAttrs);
        }
        return taskSubmitParam;
    }
}
