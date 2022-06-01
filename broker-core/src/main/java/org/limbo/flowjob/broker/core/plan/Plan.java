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
import org.limbo.flowjob.broker.core.plan.job.Job;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link Job}
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
     * 是否已启用
     */
    private boolean isEnabled;

    // --------需注入
    @ToString.Exclude
    @Inject
    private PlanInfoRepository planInfoRepository;

    @ToString.Exclude
    @Inject
    private PlanRepository planRepository;


    /**
     * 启用当前计划
     */
    public boolean enable() {
        boolean succeed = planRepository.enablePlan(this) == 1;
        if (succeed) {
            this.isEnabled = true;
        }
        return succeed;
    }


    /**
     * 停用当前计划
     */
    public boolean disable() {
        boolean succeed = planRepository.disablePlan(this) == 1;
        if (succeed) {
            this.isEnabled = false;
        }
        return succeed;
    }


    /**
     * 查询具体版本号下的计划信息
     * @param version 版本号
     * @return 计划具体信息
     */
    public PlanInfo getInfoAtVersion(Integer version) {
        return planInfoRepository.getByVersion(planId, version);
    }


    /**
     * 查询当前版本的计划执行信息
     * @return 计划具体信息
     */
    public PlanInfo getCurrentVersionInfo() {
        return planInfoRepository.getByVersion(planId, currentVersion);
    }


    /**
     * 更新执行计划信息，版本号递增
     * @param planInfo 执行计划信息
     */
    public void addNewVersion(PlanInfo planInfo) {
        // 为 PlanInfo 设置版本号
        int newVersion = getRecentlyVersion() + 1;
        planInfo.setVersion(newVersion);

        // 添加新版本信息
        planInfoRepository.addVersion(planInfo);

        // 更新当前使用版本信息
        newVersion = planRepository.updateVersion(this, newVersion);

        // 更新领域对象中的版本号
        currentVersion = newVersion;
        recentlyVersion = newVersion;
    }

}
