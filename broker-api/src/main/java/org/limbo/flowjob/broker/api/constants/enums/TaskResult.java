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
public enum TaskResult {

    NONE(0, "未执行结束"),
    FAILED(1, "执行失败"),
    SUCCEED(2, "执行成功"),
    ;

    @JsonValue
    public final byte result;

    public final String desc;

    @JsonCreator
    TaskResult(int result, String desc) {
        this(((byte) result), desc);
    }

    TaskResult(byte result, String desc) {
        this.result = result;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     * @param result 待校验值
     */
    public boolean is(TaskResult result) {
        return equals(result);
    }

    /**
     * 校验是否是当前状态
     * @param result 待校验状态值
     */
    public boolean is(Number result) {
        return result != null && result.byteValue() == this.result;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static TaskResult parse(Number result) {
        if (result == null) {
            return null;
        }

        for (TaskResult taskResult : values()) {
            if (taskResult.is(result)) {
                return taskResult;
            }
        }

        return null;
    }

}
