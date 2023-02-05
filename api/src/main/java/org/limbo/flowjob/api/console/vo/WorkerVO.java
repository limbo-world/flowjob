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

package org.limbo.flowjob.api.console.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.constants.WorkerStatus;

import java.time.LocalDateTime;

/**
 * @author KaiFengCai
 * @since 2023/1/30
 */
@Data
@Schema(title = "Worker")
public class WorkerVO {

    @Schema(title = "id")
    private String workerId;

    @Schema(title = "名称")
    private String name;

    @Schema(title = "通信协议")
    private String protocol;

    @Schema(title = "host")
    private String host;

    @Schema(title = "port")
    private Integer port;

    /**
     * worker节点状态
     * @see WorkerStatus
     */
    @Schema(title = "节点状态")
    private Byte status;

    /**
     * 是否启用 不启用则不会进行任务下发
     */
    @Schema(title = "是否启用")
    private boolean enabled;

}
