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

public enum TagFilterCondition {

    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),

    /**
     * 存在指定名称的标签
     */
    EXISTS(1, "存在指定名称的标签"),

    /**
     * 不存在指定名称的标签
     */
    NOT_EXISTS(2, "不存在指定名称的标签"),

    /**
     * 存在指定名称的标签且匹配指定值
     */
    MUST_MATCH_VALUE(3, "存在指定名称的标签且匹配指定值"),

    /**
     * 存在指定名称的标签，且不匹配指定值
     */
    MUST_NOT_MATCH_VALUE(4, "存在指定名称的标签，且不匹配指定值"),

    /**
     * 存在指定名称的标签且匹配正则表达式
     */
    MUST_MATCH_VALUE_REGEX(5, "存在指定名称的标签且匹配正则表达式"),

    /**
     * 匹配对应的host和port
     */
    MATCH_HOST_PORT(6, "匹配对应的host和port"),
    ;

    @JsonValue
    public final int condition;

    @Getter
    public final String desc;

    TagFilterCondition(int condition, String desc) {
        this.condition = condition;
        this.desc = desc;
    }

    public boolean is(Number condition) {
        return condition != null && condition.intValue() == this.condition;
    }

    @JsonCreator
    public static TagFilterCondition parse(Number condition) {
        for (TagFilterCondition tagFilterCondition : values()) {
            if (tagFilterCondition.is(condition)) {
                return tagFilterCondition;
            }
        }
        return UNKNOWN;
    }

}
