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

package org.limbo.flowjob.tracker.core.constants;

/**
 * 作业分发方式.
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum JobDispatchType {

    ROUND_ROBIN(1, "轮询"),

    RANDOM(2, "随机"),

    APPOINT(3, "指定节点"),

    LEAST_FREQUENTLY_USED(4, "最不经常使用"),

    LEAST_RECENTLY_USED(5, "最近最少使用"),

    CONSISTENT_HASH(6, "一致性hash"),

    ;


    public final int type;

    public final String desc;

    JobDispatchType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    /**
     * 解析作业分发类型。
     */
    public static JobDispatchType parse(Integer type) {
        if (type == null) {
            return null;
        }

        for (JobDispatchType dispatchType : values()) {
            if (type.equals(dispatchType.type)) {
                return dispatchType;
            }
        }

        return null;
    }

}
