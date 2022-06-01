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

package org.limbo.flowjob.broker.dao.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Converter;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.core.worker.metric.JobDescription;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.dao.po.WorkerMetricPO;
import org.limbo.utils.JacksonUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Component
public class WorkerMetricPoConverter extends Converter<WorkerMetric, WorkerMetricPO> {

    /**
     * 将{@link WorkerMetric}值对象转换为{@link WorkerMetricPO}持久化对象
     * @param vo {@link WorkerMetric}值对象
     * @return {@link WorkerMetricPO}持久化对象
     */
    @Override
    protected WorkerMetricPO doForward(WorkerMetric vo) {
        WorkerMetricPO po = new WorkerMetricPO();
        po.setWorkerId(vo.getWorkerId());

        WorkerAvailableResource availableResource = vo.getAvailableResource();
        po.setAvailableCpu(availableResource.getAvailableCpu());
        po.setAvailableRam(availableResource.getAvailableRam());
        po.setAvailableQueueLimit(availableResource.getAvailableQueueLimit());

        // 执行中的任务
        po.setExecutingJobs(JacksonUtils.toJSONString(vo.getExecutingJobs()));

        return po;
    }

    /**
     * 将{@link WorkerMetricPO}持久化对象转换为{@link WorkerMetric}值对象
     * @param po {@link WorkerMetricPO}持久化对象
     * @return {@link WorkerMetric}值对象
     */
    @Override
    protected WorkerMetric doBackward(WorkerMetricPO po) {
        WorkerMetric metric = new WorkerMetric();
        metric.setWorkerId(po.getWorkerId());
        metric.setAvailableResource(new WorkerAvailableResource(
                po.getAvailableCpu(), po.getAvailableRam(), po.getAvailableQueueLimit()
        ));
        metric.setExecutingJobs(JacksonUtils.parseObject(po.getExecutingJobs(), new TypeReference<List<JobDescription>>() {
        }));

        return metric;
    }
}
