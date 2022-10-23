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

package org.limbo.flowjob.broker.core.domain.job;

import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-10-20
 */
public class JobInstanceFactory {

    public static JobInstance create(String planInstanceId, JobInfo jobInfo, LocalDateTime triggerAt) {
        JobInstance instance = new JobInstance();
        instance.setPlanInstanceId(planInstanceId);
        instance.setJobId(jobInfo.getId());
        instance.setDispatchOption(jobInfo.getDispatchOption());
        instance.setExecutorName(jobInfo.getExecutorName());
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setAttributes(jobInfo.getAttributes());
        return instance;
    }

}
