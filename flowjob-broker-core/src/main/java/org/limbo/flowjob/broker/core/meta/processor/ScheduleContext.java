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

package org.limbo.flowjob.broker.core.meta.processor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.info.Plan;

import java.util.List;

/**
 * @author Devil
 * @since 2023/2/8
 */
@Slf4j
@Data
public class ScheduleContext {

    /**
     * 调度中后续调度的plan
     */
    private Plan waitSchedulePlan;

    /**
     * 调度中产生的需要后续下发的job
     */
    private List<JobInstance> waitScheduleJobs;

}
