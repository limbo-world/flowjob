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

package org.limbo.flowjob.api.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.common.constants.JobType;

import java.util.Set;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业")
public class JobAddParam {

    /**
     * 作业ID
     */
    @Schema(title = "作业ID")
    private String jobId;

    /**
     * 作业描述
     */
    @Schema(title = "作业描述")
    private String description;

    /**
     * 此作业相连的下级作业ID
     */
    @Schema(title = "此作业相连的下级作业ID")
    private Set<String> childrenIds;

    /**
     * 作业类型
     */
    @Schema(title = "作业类型")
    private JobType type;

    /**
     * 作业分发配置参数
     */
    @Schema(title = "作业分发配置参数")
    private DispatchOptionParam dispatchOption;

    /**
     * 执行器名称
     */
    @Schema(title = "执行器名称")
    private String executorName;
}
