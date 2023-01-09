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
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.dag.DAG;

/**
 * 计划在具体版本时的数据(值对象)，至少对应一个{@link JobInfo}
 *
 * @author Brozen
 * @since 2021-10-14
 */
@Getter
@ToString
public class WorkflowPlan extends Plan {

    /**
     * 作业计划对应的Job，以DAG数据结构组织
     */
    private final DAG<JobInfo> dag;

    public WorkflowPlan(String planId, String version,
                        TriggerType triggerType, ScheduleOption scheduleOption, DAG<JobInfo> dag) {
        super(planId, version, triggerType, scheduleOption);
        this.dag = dag;
    }

    @Override
    public PlanType planType() {
        return PlanType.WORKFLOW;
    }
}
