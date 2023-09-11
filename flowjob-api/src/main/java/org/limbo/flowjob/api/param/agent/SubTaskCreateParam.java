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

package org.limbo.flowjob.api.param.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 作业执行反馈
 *
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "子任务创建参数")
public class SubTaskCreateParam implements Serializable {

    private static final long serialVersionUID = 7622034220519608736L;

    /**
     * jobId
     */
    @Schema(description = "jobId")
    private String jobId;

    private List<SubTaskInfoParam> subTasks;

    @Data
    public static class SubTaskInfoParam implements Serializable {

        private static final long serialVersionUID = -223386710820618775L;

        /**
         * taskId
         */
        @Schema(description = "taskId")
        private String taskId;

        /**
         * 执行器名称
         */
        @Schema(description = "执行器名称")
        private String executorName;

        /**
         * 返回的数据
         */
        @Schema(description = "返回的数据")
        private Map<String, Object> data;
    }

}
