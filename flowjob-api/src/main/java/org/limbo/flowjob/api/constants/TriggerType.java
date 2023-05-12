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
 * 触发类型：
 * <ul>
 *     <li>{@linkplain TriggerType#API api触发}</li>
 *     <li>{@linkplain TriggerType#SCHEDULE 调度触发}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum TriggerType {

    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),
    /**
     * 调用api触发
     */
    API(1, "api触发"),
    /**
     * 通过调度触发
     */
    SCHEDULE(2, "调度触发"),

    ;

    @JsonValue
    public final int type;

    @Getter
    public final String desc;


    TriggerType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public boolean is(Number type) {
        return type != null && type.intValue() == this.type;
    }

    @JsonCreator
    public static TriggerType parse(Number type) {
        for (TriggerType triggerType : values()) {
            if (triggerType.is(type)) {
                return triggerType;
            }
        }
        return UNKNOWN;
    }

}
