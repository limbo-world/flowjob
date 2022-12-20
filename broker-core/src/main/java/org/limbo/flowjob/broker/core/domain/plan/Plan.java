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
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.LoopMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.core.service.IScheduleService;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link JobInfo}
 * 主要是对plan的管理
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@Setter
@ToString
public class Plan extends LoopMetaTask implements Serializable {

    private static final long serialVersionUID = 5657376836197403211L;

    /**
     * 执行计划ID
     */
    private String planId;

    /**
     * 当前版本
     */
    private Integer currentVersion;

    /**
     * 最新版本
     */
    private Integer recentlyVersion;

    /**
     * 当前版本的Plan数据
     */
    private PlanInfo info;

    /**
     * 是否已启用
     */
    private boolean enabled;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private IScheduleService iScheduleService;

    public Plan(String planId, Integer currentVersion, Integer recentlyVersion, LocalDateTime triggerAt, PlanInfo info, boolean enabled,
                IScheduleService iScheduleService, MetaTaskScheduler metaTaskScheduler) {
        super(triggerAt, info.getScheduleOption(), metaTaskScheduler);
        this.planId = planId;
        this.currentVersion = currentVersion;
        this.recentlyVersion = recentlyVersion;
        this.info = info;
        this.enabled = enabled;
        this.iScheduleService = iScheduleService;
    }

    @Override
    protected void executeTask() {
        iScheduleService.schedule(this);
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN;
    }

    @Override
    public String getMetaId() {
        return planId + "-" + currentVersion;
    }

}
