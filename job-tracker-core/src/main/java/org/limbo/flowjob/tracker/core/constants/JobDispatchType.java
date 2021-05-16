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

    ROUND_ROBIN(1, "轮训"),

    RANDOM(2, "随机"),

    N_TH(3, "第N个"),

    CONSISTENT_HASH(4, "一致性hash"),

    FAIL_OVER(5, "故障转移"),

    BUSY_OVER(6, "忙碌转移"),

    SHARD_BROADCAST(7, "分片广播"),

    MAP_REDUCE(8, "MapReduce"),

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
