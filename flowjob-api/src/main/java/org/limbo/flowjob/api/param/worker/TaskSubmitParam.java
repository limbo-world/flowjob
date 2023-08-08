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

package org.limbo.flowjob.api.param.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.api.constants.TaskType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 下发任务
 * @author Devil
 * @since 2021/7/24
 */
@Data
public class TaskSubmitParam implements Serializable {

    private static final long serialVersionUID = -5172349572814593252L;

    @NotBlank
    private String taskId;

    private String agentId;

    /**
     * Task类型
     * @see TaskType
     */
    @NotNull(message = "type can't be null")
    @Schema(description = "Task类型")
    private Integer type;

    /**
     * 执行器的名称
     */
    private String executorName;

    /**
     * 上下文元数据
     */
    private Map<String, Object> context;

    /**
     * job配置的属性
     */
    private Map<String, Object> attributes;

    /**
     * 每个map task单独的属性
     */
    private Map<String, Object> mapAttributes;

    /**
     * reduce时候使用的
     */
    private List<Map<String, Object>> reduceAttributes;

}
