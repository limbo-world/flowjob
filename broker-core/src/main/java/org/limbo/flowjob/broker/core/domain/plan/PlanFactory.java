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

import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.service.IScheduleService;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2022/11/26
 */
public class PlanFactory {

    private final IDGenerator idGenerator;

    private final IScheduleService iScheduleService;

    private final MetaTaskScheduler metaTaskScheduler;

    public PlanFactory(IDGenerator idGenerator, IScheduleService iScheduleService, MetaTaskScheduler metaTaskScheduler) {
        this.idGenerator = idGenerator;
        this.iScheduleService = iScheduleService;
        this.metaTaskScheduler = metaTaskScheduler;
    }

    public Plan create(LocalDateTime triggerAt, String description, ScheduleOption scheduleOption, DAG<JobInfo> dag, boolean enabled) {
        String planId = idGenerator.generateId(IDType.PLAN);
        Integer version = 1;
        PlanInfo info = new PlanInfo(planId, version, description, scheduleOption, dag);
        return new Plan(planId, version, version, triggerAt, info, enabled, iScheduleService, metaTaskScheduler);
    }

    public Plan newVersion(Plan oldPlan, String description, ScheduleOption scheduleOption, DAG<JobInfo> dag) {
        PlanInfo oldInfo = oldPlan.getInfo();
        Integer newVersion = oldInfo.getVersion() + 1;

        PlanInfo newInfo = new PlanInfo(oldPlan.getPlanId(), newVersion, description, scheduleOption, dag);

        return new Plan(oldPlan.getPlanId(), newVersion, newVersion, oldPlan.nextTriggerAt(), newInfo, oldPlan.isEnabled(), iScheduleService, metaTaskScheduler);
    }

    /**
     * 生成新的计划调度记录
     *
     * @param triggerType 触发类型
     * @return 调度记录状态
     */
    public PlanInstance newInstance(PlanInfo planInfo, TriggerType triggerType, LocalDateTime triggerAt) {
        PlanInstance instance = new PlanInstance();
        instance.setPlanInstanceId(idGenerator.generateId(IDType.PLAN_INSTANCE));
        instance.setPlanId(planInfo.getPlanId());
        instance.setVersion(planInfo.getVersion());
        instance.setDag(planInfo.getDag());
        instance.setStatus(PlanStatus.SCHEDULING);
        instance.setTriggerType(triggerType);
        instance.setScheduleOption(planInfo.getScheduleOption());
        instance.setTriggerAt(triggerAt);
//        instance.setTriggerAt(nextTriggerAt); // 新建的时候不需要
//        instance.setContext(); // todo
        return instance;
    }

}
