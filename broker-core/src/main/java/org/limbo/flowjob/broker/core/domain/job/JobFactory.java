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

import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.common.constants.JobStatus;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2022/11/26
 */
public class JobFactory {

    private final IDGenerator idGenerator;

    public JobFactory(IDGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public JobInstance newInstance(PlanInstance planInstance, JobInfo jobInfo, LocalDateTime triggerAt) {
        JobInstance instance = new JobInstance();
        instance.setJobInstanceId(idGenerator.generateId(IDType.JOB_INSTANCE));
        instance.setPlanInstanceId(planInstance.getPlanInstanceId());
        instance.setPlanVersion(planInstance.getVersion());
        instance.setPlanId(planInstance.getPlanId());
        instance.setJobId(jobInfo.getId());
        instance.setDispatchOption(jobInfo.getDispatchOption());
        instance.setExecutorName(jobInfo.getExecutorName());
        instance.setType(jobInfo.getType());
        instance.setTriggerType(jobInfo.getTriggerType());
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setAttributes(jobInfo.getAttributes());
        instance.setTerminateWithFail(jobInfo.isTerminateWithFail());
        return instance;
    }


}
