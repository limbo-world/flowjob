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

package org.limbo.flowjob.broker.api.console.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;

/**
 * @author Devil
 * @since 2021/7/28
 */
@Data
@Schema(title = "执行器配置参数")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorOptionParam {

    /**
     * 执行器名称
     */
    @Schema(title = "执行器名称", description = "执行器名称")
    private String name;

    /**
     * 执行器类型
     */
    @Schema(title = "执行器类型", description = "执行器类型")
    private JobExecuteType type;

}
