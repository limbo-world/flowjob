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
import org.limbo.flowjob.common.constants.ExecuteResult;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 作业执行反馈
 *
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业执行反馈参数")
public class TaskFeedbackParam {

    /**
     * 任务ID
     */
    @NotBlank(message = "taskId can't be null")
    @Schema(description = "任务ID")
    private String taskId;

    /**
     * 执行结果
     * @see ExecuteResult
     */
    @NotNull(message = "result can't be null")
    @Schema(description = "执行结果，参考枚举 ExecuteResult")
    private Integer result;

    /**
     * 执行失败时候返回的信息
     */
    @Schema(description = "执行失败时候返回的信息")
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    @Schema(description = "执行失败时的异常堆栈")
    private String errorStackTrace;

    /**
     * 更新的作业上下文元数据
     */
    @Schema(description = "更新的作业上下文元数据")
    private Map<String, Object> context;

    /**
     * 子任务属性
     */
    @Schema(description = "子任务属性")
    private List<Map<String, Object>> subTaskAttributes;

    /**
     * 返回的数据
     */
    @Schema(description = "返回的数据")
    private Map<String, Object> resultAttributes;

}
