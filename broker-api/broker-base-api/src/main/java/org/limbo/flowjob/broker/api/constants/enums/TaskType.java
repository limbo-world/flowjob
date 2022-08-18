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
 * @author Brozen
 * @since 2021-05-19
 */
public enum TaskType {
    /**
     * 普通任务
     */
    NORMAL(1, "普通任务"),
    /**
     * 广播任务
     */
    BROADCAST(2, "广播任务"),
    /**
     * 用于拆分任务到子任务
     */
    MAP(3, "map任务"),
    /**
     * 子任务
     */
    SUB(4, "子任务"),
    /**
     * 子任务归纳合并的任务
     */
    REDUCE(5, "Reduce任务"),
    ;

    @JsonValue
    public final byte type;

    public final String desc;

    @JsonCreator
    TaskType(int type, String desc) {
        this(((byte) type), desc);
    }

    TaskType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     * @param type 待校验值
     */
    public boolean is(TaskType type) {
        return equals(type);
    }

    /**
     * 校验是否是当前状态
     * @param type 待校验状态值
     */
    public boolean is(Number type) {
        return type != null && type.byteValue() == this.type;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static TaskType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (TaskType jobType : values()) {
            if (jobType.is(type)) {
                return jobType;
            }
        }

        return null;
    }

}
