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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.LoopMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.core.schedule.strategy.IScheduleStrategy;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link JobInfo}
 * 主要是对plan的管理
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Slf4j
@Getter
@ToString
public class Plan extends LoopMetaTask implements Serializable {

    private static final long serialVersionUID = 5657376836197403211L;

    /**
     * 执行计划ID
     */
    private final String planId;

    /**
     * 计划信息
     */
    private final PlanInfo planInfo;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private IScheduleStrategy iScheduleStrategy;

    public Plan(PlanInfo planInfo, ScheduleOption scheduleOption,
                LocalDateTime lastTriggerAt, LocalDateTime lastFeedbackAt,
                IScheduleStrategy iScheduleStrategy, MetaTaskScheduler metaTaskScheduler) {
        super(lastTriggerAt, lastFeedbackAt, scheduleOption, metaTaskScheduler);
        this.planId = planInfo.getPlanId();
        this.planInfo = planInfo;
        this.iScheduleStrategy = iScheduleStrategy;
    }

    @Override
    public void execute() {
        ScheduleOption scheduleOption = getScheduleOption();
        if (scheduleOption == null || scheduleOption.getScheduleType() == null || ScheduleType.UNKNOWN == scheduleOption.getScheduleType()) {
            log.error("{} scheduleType is {} scheduleOption={}", scheduleId(), MsgConstants.UNKNOWN, scheduleOption);
            return;
        }
        switch (getScheduleOption().getScheduleType()) {
            case FIXED_RATE:
            case CRON:
                executeFixedRate();
                break;
            default:
                // FIXED_DELAY 交由执行完后处理
                break;
        }
    }

    @Override
    protected void executeTask() {
        iScheduleStrategy.schedule(TriggerType.SCHEDULE, this);
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN;
    }

    @Override
    public String getMetaId() {
        return planId + "-" + planInfo.getVersion();
    }

}
