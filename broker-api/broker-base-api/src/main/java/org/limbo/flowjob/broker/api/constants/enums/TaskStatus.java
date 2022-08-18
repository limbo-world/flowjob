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
 * 任务下发状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum TaskStatus {
    /**
     * 任务刚创建，还在内存，未下发给worker
     */
    DISPATCHING(1, "下发中"),

    /**
     * 任务已经创建且持久化，下发给worker成功，正在执行中
     */
    EXECUTING(3, "执行中"),

    /**
     * 执行成功
     */
    SUCCEED(4, "执行成功"),

    /**
     * 执行失败
     */
    FAILED(5, "执行失败"),
    ;

    @JsonValue
    public final byte status;

    public final String desc;

    @JsonCreator
    TaskStatus(int status, String desc) {
        this(((byte) status), desc);
    }

    TaskStatus(byte status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(TaskStatus status) {
        return equals(status);
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(Number status) {
        return status != null && status.byteValue() == this.status;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static TaskStatus parse(Number status) {
        if (status == null) {
            return null;
        }

        for (TaskStatus statusEnum : values()) {
            if (statusEnum.is(status)) {
                return statusEnum;
            }
        }

        return null;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return this == TaskStatus.SUCCEED || this == TaskStatus.FAILED;
    }

}
