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

package org.limbo.flowjob.broker.application.plan.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.utils.UUIDUtils;
import org.springframework.stereotype.Component;

/**
 * @author Brozen
 * @since 2022-08-29
 */
@Component
public class WorkerFactory {

    /**
     * 生成新的worker，根据注册参数创建
     * @param options worker 注册参数
     * @return worker领域对象
     */
    public Worker newWorker(WorkerRegisterParam options) {
        String workerId = StringUtils.isNotBlank(options.getId()) ? options.getId() : UUIDUtils.randomID();
        return Worker.builder()
                .workerId(workerId)
                .rpcBaseUrl(options.getUrl())
                .executors(Lists.newArrayList())
                .tags(Maps.newHashMap())
                .metric(new WorkerMetric(workerId))
                .status(WorkerStatus.TERMINATED)
                .build();
    }


}
