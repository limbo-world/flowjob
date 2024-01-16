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

package org.limbo.flowjob.broker.core.meta.instance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.limbo.flowjob.api.constants.InstanceStatus;
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2024/1/4
 */
@Getter
@AllArgsConstructor
public abstract class Instance {

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 类型
     */
    private InstanceType type;

    /**
     * 状态
     */
    private InstanceStatus status;

    /**
     * 作业计划对应的Job，以DAG数据结构组织
     */
    private DAG<WorkflowJobInfo> dag;

    /**
     * 属性
     */
    private Attributes attributes;

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

}
