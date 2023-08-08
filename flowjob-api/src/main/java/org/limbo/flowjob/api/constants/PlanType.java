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
 * 计划类型
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum PlanType {
    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),
    STANDALONE(1, "单例任务"),
    WORKFLOW(2, "工作流任务"),
    ;

    @JsonValue
    public final int type;

    public final String desc;

    @JsonCreator
    PlanType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public boolean is(PlanType type) {
        return equals(type);
    }

    public boolean is(Number type) {
        return type != null && type.intValue() == this.type;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static PlanType parse(Number type) {
        for (PlanType planType : values()) {
            if (planType.is(type)) {
                return planType;
            }
        }
        return UNKNOWN;
    }

}
