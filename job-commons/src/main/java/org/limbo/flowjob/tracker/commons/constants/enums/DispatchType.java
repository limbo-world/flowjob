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

package org.limbo.flowjob.tracker.commons.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/**
 * 负载方式.
 * <ul>
 *     <li>{@linkplain DispatchType#SINGLE 单点}</li>
 *     <li>{@linkplain DispatchType#BROADCAST 广播}</li>
 *     <li>{@linkplain DispatchType#SHARDING 分片}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum DispatchType implements DescribableEnum<Byte> {


    /**
     * 单点
     */
    SINGLE(1, "单点"),

    /**
     * 广播
     */
    BROADCAST(2, "广播"),

    /**
     * 分片
     */
    SHARDING(3, "分片"),

    ;

    /**
     * 可通过{@link #describe(Class)}方法生成，用于在swagger3为枚举添加说明
     */
    public static final String DESCRIPTION = "1-单点; 2-广播; 3-分片;";

    /**
     * 分发类型值
     */
    public final byte type;

    /**
     * 分发类型描述
     */
    @Getter
    public final String desc;

    DispatchType(int type, String desc) {
        this((byte) type, desc);
    }

    DispatchType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Byte getValue() {
        return type;
    }

    /**
     * 解析作业分发类型。
     */
    @JsonCreator
    public static DispatchType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (DispatchType dispatchType : values()) {
            if (type.byteValue() == dispatchType.type) {
                return dispatchType;
            }
        }

        return null;
    }

}
