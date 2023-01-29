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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 一个调度的plan实例
 *
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public class PlanInstance implements Serializable {

    private static final long serialVersionUID = 1837382860200548371L;

    private String planInstanceId;

    private String planId;

    /**
     * 计划的版本
     */
    private String version;

    /**
     * 计划调度状态
     */
    private PlanStatus status;

    /**
     * 触发类型，触发本次 Plan 的方式
     */
    private TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 期望的调度触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 执行开始时间
     */
    private LocalDateTime startAt;

    /**
     * 执行结束时间
     */
    private LocalDateTime feedbackAt;

    /**
     * 执行图
     */
    private DAG<WorkflowJobInfo> dag;

    /**
     * 全局上下文 配置 --- 或者job执行中变更 整个plan生命周期内传递
     */
    private Attributes context;

}
