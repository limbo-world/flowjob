/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.api.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Worker的状态
 */
public enum WorkerStatus {

    /**
     * Worker正常运行中
     */
    RUNNING(1),

    /**
     * Worker熔断中，此状态的Worker无法接受作业，并将等待心跳重连并复活。
     */
    FUSING(2),

    /**
     * Worker已停止。
     */
    TERMINATED(3),

    ;

    @JsonValue
    public final byte status;

    WorkerStatus(int status) {
        this(((byte) status));
    }

    WorkerStatus(byte status) {
        this.status = status;
    }

    /**
     * 解析worker状态
     */
    @JsonCreator
    public static WorkerStatus parse(Number status) {
        if (status == null) {
            return null;
        }

        for (WorkerStatus workerStatus : values()) {
            if (status.byteValue() == workerStatus.status) {
                return workerStatus;
            }
        }

        return null;
    }
}
