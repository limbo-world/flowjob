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
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.plan.job.JobInfo;
import org.limbo.flowjob.broker.core.plan.job.dag.DAG;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.TimeUtil;

import java.io.Serializable;

/**
 * 计划在具体版本时的数据(值对象)，至少对应一个{@link JobInfo}
 *
 * @author Brozen
 * @since 2021-10-14
 */
@Getter
@Setter
@ToString
public class PlanInfo implements Serializable {

    private static final long serialVersionUID = -3488415933872953356L;

    /**
     * 作业执行计划ID
     */
    private String planId;

    /**
     * 版本 planId + version 唯一
     */
    private String version;

    /**
     * 执行计划描述
     */
    private String description;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 作业计划对应的Job，以DAG数据结构组织
     */
    private DAG<JobInfo> dag;


    public PlanInfo(String planId, String version, String description,
                    ScheduleOption scheduleOption, DAG<JobInfo> dag) {
        this.planId = planId;
        this.version = version;
        this.description = description;
        this.scheduleOption = scheduleOption;
        this.dag = dag;
    }


    /**
     * 生成新的计划调度记录
     *
     * @param status       初始化调度记录状态
     * @param triggerType 触发类型
     * @return 调度记录状态
     */
    public PlanInstance newInstance(PlanScheduleStatus status, TriggerType triggerType) {
        PlanInstance instance = new PlanInstance();
        instance.setPlanId(planId);
        instance.setVersion(version);
        instance.setDag(dag);
        instance.setStatus(status);
        instance.setTriggerType(triggerType);
        instance.setScheduleAt(TimeUtil.nowLocalDateTime());
        return instance;
    }

    /**
     * 校验info的正确性
     */
    public void check() {
        // todo
    }

}
