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
 * 作业执行方式 todo 这个是否移除 shell 也是一种特定的function
 *
 * @author Brozen
 * @since 2021-07-05
 */
public enum JobExecuteType {

    /**
     * 非法值
     */
    UNKNOWN(0, "未知执行方式"),

    /**
     * 通过 function 执行任务，worker中有能够执行任务的 function，接收到任务后，执行指定逻辑
     */
    FUNCTION(1, "通过 worker 指定的 function 执行执行任务"),

    /**
     * 通过shell执行任务，shell脚本下发到worker后，worker在本地执行脚本
     */
    SHELL(2, "shell脚本任务"),

    ;

    /**
     * 执行方式枚举值
     */
    @JsonValue
    public final byte type;

    /**
     * 执行方式描述
     */
    public final String desc;

    JobExecuteType(int type, String desc) {
        this(((byte) type), desc);
    }

    JobExecuteType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    /**
     * 解析作业执行方式。
     * @return 解析得到的作业执行方式枚举，解析失败则返回{@link #UNKNOWN}
     */
    @JsonCreator
    public static JobExecuteType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (JobExecuteType executeType : values()) {
            if (type.byteValue() == executeType.type) {
                return executeType;
            }
        }

        return UNKNOWN;
    }

}
