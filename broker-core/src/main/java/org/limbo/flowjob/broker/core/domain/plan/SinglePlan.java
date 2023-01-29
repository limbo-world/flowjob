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

package org.limbo.flowjob.broker.core.domain.plan;

import lombok.Getter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TriggerType;

/**
 * 计划在具体版本时的数据(值对象)，至少对应一个{@link WorkflowJobInfo}
 *
 * @author Brozen
 * @since 2021-10-14
 */
@Getter
@ToString
public class SinglePlan extends Plan {

    /**
     * 任务信息
     */
    private final JobInfo jobInfo;

    public SinglePlan(String planId, String version, TriggerType triggerType,
                      ScheduleOption scheduleOption, JobInfo jobInfo) {
        super(planId, version, triggerType, scheduleOption);
        this.jobInfo = jobInfo;
    }

    @Override
    public PlanType planType() {
        return PlanType.SINGLE;
    }
}
