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

package org.limbo.flowjob.broker.core.domain.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.common.constants.NodeType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.dag.DAGNodeIgnoreField;

import java.util.Set;

/**
 * 作业的抽象。主要定义了作业领域的的行为方法，属性的访问操作在{@link WorkflowJobInfo}轻量级领域对象中。
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class WorkflowJobInfo extends DAGNode {

    private static final long serialVersionUID = -702096482598918849L;

    /**
     * 名称 视图里面唯一
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 节点类型 todo ???
     */
    private NodeType nodeType;

    /**
     * 触发类型
     */
    private TriggerType triggerType;

    /**
     * 执行失败是否终止 false 会继续执行后续作业
     */
    private boolean terminateWithFail;

    @DAGNodeIgnoreField
    private JobInfo job;

    @JsonCreator
    public WorkflowJobInfo(@JsonProperty("id") String id, @JsonProperty("childrenIds") Set<String> childrenIds) {
        super(id, childrenIds);
    }

}
