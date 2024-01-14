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

import lombok.Builder;
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
public class DelayInstance extends Instance {

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID type + id 唯一
     */
    private String bizId;

    @Builder
    public DelayInstance(String id, InstanceType type, InstanceStatus status, Attributes attributes, LocalDateTime triggerAt, LocalDateTime startAt, LocalDateTime feedbackAt, String bizType, String bizId, DAG<WorkflowJobInfo> dag) {
        super(id, type, status, dag, attributes, triggerAt, startAt, feedbackAt);
        this.bizType = bizType;
        this.bizId = bizId;
    }
}
