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
import lombok.EqualsAndHashCode;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.param.console.DispatchOptionParam;
import org.limbo.flowjob.api.param.console.OvertimeOptionParam;
import org.limbo.flowjob.api.param.console.RetryOptionParam;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/8/17
 */
@Data
public class DelayInstanceCommitParam implements Serializable {

    private static final long serialVersionUID = -6987120924376305181L;
    /**
     * 主题
     */
    private String bizType;

    /**
     * 业务ID type + id 唯一
     */
    private String bizId;

    /**
     * 触发时间
     */
    private LocalDateTime triggerAt;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(title = "普通任务参数")
    public static class StandaloneParam extends DelayInstanceCommitParam {

        private static final long serialVersionUID = -6883938273568167064L;
        /**
         * 作业类型
         *
         * @see JobType
         */
        @NotNull
        @Schema(title = "作业类型")
        private Integer type;

        /**
         * 属性参数
         */
        @Schema(title = "属性参数")
        private Map<String, Object> attributes;

        /**
         * 作业超时参数
         */
        @Schema(title = "作业超时参数")
        private OvertimeOptionParam overtimeOption;

        /**
         * 作业分发重试参数
         */
        @Schema(title = "作业分发重试参数")
        private RetryOptionParam retryOption;

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
    }

}
