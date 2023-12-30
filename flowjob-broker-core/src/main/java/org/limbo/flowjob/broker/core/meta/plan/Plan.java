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

package org.limbo.flowjob.broker.core.meta.plan;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.meta.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link WorkflowJobInfo}
 * 主要是对plan的管理
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@Setter(AccessLevel.NONE)
@ToString
@Builder(builderClassName = "Builder")
public class Plan implements Serializable {

    private static final long serialVersionUID = 5657376836197403211L;

    /**
     * 作业执行计划ID
     */
    private final String id;

    /**
     * 版本
     */
    private final String version;

    /**
     * 类型
     */
    private final PlanType type;

    /**
     * 触发类型
     */
    private final TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    private final ScheduleOption scheduleOption;

    /**
     * 作业计划对应的Job，以DAG数据结构组织
     */
    private final DAG<WorkflowJobInfo> dag;

    /**
     * 上次触发时间
     */
    private final LocalDateTime latelyTriggerAt;

    /**
     * 上次回调时间
     */
    private final LocalDateTime latelyFeedbackAt;

    /**
     * 对应的broker
     */
    private final URL brokerUrl;

    /**
     * 是否启用
     */
    private final boolean enabled;


}
