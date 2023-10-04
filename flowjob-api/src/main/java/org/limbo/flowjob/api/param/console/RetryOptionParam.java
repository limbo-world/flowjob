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

package org.limbo.flowjob.api.param.console;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.limbo.flowjob.api.constants.RetryType;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业重试参数")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryOptionParam {

    /**
     * 重试次数
     */
    @Schema(title = "重试次数")
    private Integer retry;

    /**
     * 重试间隔
     */
    @Schema(title = "重试间隔-秒")
    private Integer retryInterval;

    /**
     * 重试方式
     */
    @Schema(title = "重试方式")
    private Integer retryType = RetryType.ALL.getType();

}
