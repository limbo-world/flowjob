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

package org.limbo.flowjob.api.param.broker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.api.constants.Protocol;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * worker注册时的参数
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Data
@Schema(title = "agent 注册参数")
public class AgentRegisterParam implements Serializable {

    private static final long serialVersionUID = 5051739125647679235L;

    /**
     * 通信使用的 URL。
     * @see Protocol 需要使用指定类型的协议
     */
    @NotNull(message = "通信 URL 不可为空")
    @Schema(description = "通信使用的 URL", implementation = String.class)
    private URL url;

    /**
     * 可用资源
     */
    @Schema(description = "可用资源")
    private AgentResourceParam availableResource;

}
