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

package org.limbo.flowjob.api.console.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业参数")
public class WorkflowJobParam {

    /**
     * id
     */
    @NotBlank
    @Schema(title = "id", description = "视图中唯一")
    private String id;

    /**
     * 作业名称
     */
    @NotBlank
    @Schema(title = "作业名称")
    private String name;

    /**
     * 作业描述
     */
    @Schema(title = "作业描述")
    private String description;

    /**
     * 此作业相连的下级作业名称
     */
    @Schema(title = "此作业相连的下级作业名称")
    private Set<String> children;

    /**
     * 作业类型
     */
    @NotNull
    @Schema(title = "作业类型")
    private JobType type;

    /**
     * 触发类型
     */
    @NotNull
    @Schema(title = "触发类型")
    private TriggerType triggerType;

    /**
     * 属性参数
     */
    @Valid
    @NotEmpty
    @Schema(title = "属性参数")
    private Map<String, Object> attributes;

    /**
     * 作业分发配置参数
     */
    @Valid
    @NotNull
    @Schema(title = "作业分发配置参数")
    private DispatchOptionParam dispatchOption;

    /**
     * 执行器名称
     */
    @NotBlank
    @Schema(title = "执行器名称")
    private String executorName;

    /**
     * 执行失败是否终止 false 会继续执行后续作业
     */
    @Schema(title = "执行失败是否终止", description = "false 会继续执行后续作业")
    private boolean terminateWithFail = true;
}
