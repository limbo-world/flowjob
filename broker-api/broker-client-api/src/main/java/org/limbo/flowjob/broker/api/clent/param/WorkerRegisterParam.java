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

package org.limbo.flowjob.broker.api.clent.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.limbo.flowjob.broker.api.constants.enums.Protocol;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * worker注册时的参数
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Data
@Schema(title = "worker 注册参数")
public class WorkerRegisterParam implements Serializable {

    private static final long serialVersionUID = 4234037520144789567L;

    /**
     * 注册时指定的 worker id
     */
    @Schema(description = "注册时指定的 worker id")
    private String id;

    /**
     * worker 通信使用的 URL。
     * @see Protocol 需要使用指定类型的协议
     */
    @NotNull(message = "worker 通信 URL 不可为空")
    @Schema(description = "worker 通信使用的 URL", implementation = String.class)
    private URL url;

    /**
     * worker 可用的资源
     */
    @Schema(description = "worker 可用的资源")
    private WorkerResourceParam availableResource;

    /**
     * worker 的标签
     */
    @Schema(description = "worker 的标签")
    private Set<Tag> tags;

    /**
     * 执行器
     */
    @NotEmpty(message = "worker executor can't be empty")
    @Schema(description = "执行器")
    private List<WorkerExecutorRegisterParam> executors;

    /**
     * worker所属租户信息
     */
    @Schema(description = "worker所属租户信息")
    private WorkerTenantParam tenant;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "worker 标签")
    public static class Tag {

        /**
         * 标签 key
         */
        @Schema(description = "标签 key")
        private String key;

        /**
         * 标签 value
         */
        @Schema(description = "标签 value")
        private String value;

    }

}
