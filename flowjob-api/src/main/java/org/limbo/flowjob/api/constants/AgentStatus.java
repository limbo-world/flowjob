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

package org.limbo.flowjob.api.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Agent的状态
 */
public enum AgentStatus {

    UNKNOWN(ConstantsPool.UNKNOWN),

    /**
     * 正常运行中
     */
    RUNNING(1),

    /**
     * 熔断中，此状态无法接受作业，并将等待心跳重连并复活。
     */
    FUSING(2),

    /**
     * 已停止。
     */
    TERMINATED(3),

    ;

    @JsonValue
    public final int status;

    AgentStatus(int status) {
        this.status = status;
    }

    public boolean is(Number status) {
        return status != null && status.intValue() == this.status;
    }

    /**
     * 解析
     */
    @JsonCreator
    public static AgentStatus parse(Number status) {
        for (AgentStatus statusEnum : values()) {
            if (statusEnum.is(status)) {
                return statusEnum;
            }
        }
        return UNKNOWN;
    }
}
