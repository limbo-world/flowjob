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

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "任务上报参数")
public class TaskReportParam implements Serializable {

    private static final long serialVersionUID = 8653955115178778780L;

    /**
     * jobId
     */
    @Schema(description = "jobId")
    private String jobId;

    /**
     * taskId
     */
    @Schema(description = "taskId")
    private String taskId;

}
