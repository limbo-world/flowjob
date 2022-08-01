/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.plan;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.plan.job.JobInfo;
import org.limbo.flowjob.broker.core.repository.PlanRepository;

import javax.inject.Inject;
import java.io.Serializable;

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
public class Plan implements Serializable {

    private static final long serialVersionUID = 5657376836197403211L;

    /**
     * 执行计划ID
     */
    private String planId;

    /**
     * 当前版本，版本号就是 PlanInfo 实体的ID，
     */
    private String currentVersion;

    /**
     * 最新版本
     */
    private String recentlyVersion;

    /**
     * 下次触发时间
     */
    private long nextTriggerAt;

    /**
     * 当前版本的Plan数据
     */
    private PlanInfo info;

    /**
     * 是否已启用
     */
    private boolean enabled;

    // --------需注入
    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private transient PlanRepository planRepository;


    /**
     * 启用当前计划
     */
    public boolean enable() {
        boolean succeed = planRepository.enablePlan(this);
        if (succeed) {
            this.enabled = true;
        }
        return succeed;
    }


    /**
     * 停用当前计划
     */
    public boolean disable() {
        boolean succeed = planRepository.disablePlan(this);
        if (succeed) {
            this.enabled = false;
        }
        return succeed;
    }

    /**
     * 更新执行计划信息，版本号递增
     * @param planInfo 执行计划信息
     * @return 新增的版本号
     */
    public String newVersion(PlanInfo planInfo) {
        info = planInfo;

        // 更新当前使用版本信息
        String newVersion = planRepository.updateVersion(this);

        // 更新领域对象中的版本号
        currentVersion = newVersion;
        recentlyVersion = newVersion;
        return newVersion;
    }

}
