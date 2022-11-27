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
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link JobInfo}
 * 主要是对plan的管理
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@Setter
@ToString
public class Plan implements Serializable {

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
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;

    /**
     * 当前版本的Plan数据
     */
    private PlanInfo info;

    /**
     * 是否已启用
     */
    private boolean enabled;

    public Plan(String planId, Integer currentVersion, Integer recentlyVersion, LocalDateTime nextTriggerAt, PlanInfo info, boolean enabled) {
        this.planId = planId;
        this.currentVersion = currentVersion;
        this.recentlyVersion = recentlyVersion;
        this.nextTriggerAt = nextTriggerAt;
        this.info = info;
        this.enabled = enabled;
    }
}
