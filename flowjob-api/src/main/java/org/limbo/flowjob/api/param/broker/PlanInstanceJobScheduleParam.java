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

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Devil
 * @since 2023/8/17
 */
@Data
public class PlanInstanceJobScheduleParam implements Serializable {

    private static final long serialVersionUID = 5466805029009657155L;

    @NotBlank(message = "no instanceId")
    private String instanceId;

    @NotBlank(message = "no jobId")
    private String jobId;

    // 用于指定执行节点
//    private String workerId;

//    private Map<String, Object> attributes;

}
