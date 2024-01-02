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

package org.limbo.flowjob.api.dto.console;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author KaiFengCai
 * @since 2023/1/30
 */
@Data
@Schema(title = "任务对象")
public class PlanVersionDTO {

    @Schema(title = "ID")
    private String planInfoId;

    @Schema(title = "任务名称")
    private String name;

    @Schema(title = "创建时间")
    private Long createdAt;

}
