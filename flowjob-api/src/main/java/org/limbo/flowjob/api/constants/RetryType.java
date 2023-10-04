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
import lombok.Getter;

/**
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum RetryType {

    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),
    /**
     * 对于广播/map-map/reduce任务 会重新重试
     */
    ALL(1, "重试所有"),
    /**
     * 对于广播/map-map/reduce任务 只处理失败的任务
     */
    ONLY_FAIL_PART(2, "失败部分重试"),
    ;

    @JsonValue
    @Getter
    public final int type;

    @Getter
    public final String desc;

    RetryType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     *
     * @param type 待校验值
     */
    public boolean is(RetryType type) {
        return equals(type);
    }

    /**
     * 校验是否是当前状态
     *
     * @param type 待校验状态值
     */
    public boolean is(Number type) {
        return type != null && type.intValue() == this.type;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static RetryType parse(Number type) {
        for (RetryType jobType : values()) {
            if (jobType.is(type)) {
                return jobType;
            }
        }
        return UNKNOWN;
    }

}
