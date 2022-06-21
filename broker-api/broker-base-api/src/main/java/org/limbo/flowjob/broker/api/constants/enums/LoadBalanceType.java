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
import lombok.Getter;

/**
 * 负载方式.
 * <ul>
 *     <li>{@linkplain LoadBalanceType#RANDOM 随机}</li>
 *     <li>{@linkplain LoadBalanceType#ROUND_ROBIN 轮询}</li>
 *     <li>{@linkplain LoadBalanceType#APPOINT 指定节点}</li>
 *     <li>{@linkplain LoadBalanceType#LEAST_FREQUENTLY_USED 最不经常使用}</li>
 *     <li>{@linkplain LoadBalanceType#LEAST_RECENTLY_USED 最近最少使用}</li>
 *     <li>{@linkplain LoadBalanceType#CONSISTENT_HASH 一致性hash}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum LoadBalanceType implements DescribableEnum<Byte> {


    /**
     * 随机。将作业随机下发给某一个worker执行
     */
    RANDOM(1, "随机"),

    /**
     * 轮询。
     */
    ROUND_ROBIN(2, "轮询"),

    /**
     * 指定节点。通过标签，让作业指定下发到某个worker执行。
     */
    APPOINT(3, "指定节点"),

    /**
     * 最不经常使用。将作业下发给一个时间窗口内，接收作业最少的worker。
     */
    LEAST_FREQUENTLY_USED(4, "最不经常使用"),

    /**
     * 最近最少使用。将作业下发给一个时间窗口内，最长时间没有接受worker的worker。
     */
    LEAST_RECENTLY_USED(5, "最近最少使用"),

    /**
     * 一致性hash。同样参数的作业将始终下发给同一台机器。
     */
    CONSISTENT_HASH(6, "一致性hash"),

    ;

    /**
     * 可通过{@link #describe(Class)}方法生成，用于在swagger3为枚举添加说明
     */
    public static final String DESCRIPTION = "1-轮询; 2-随机; 3-指定节点; 4-最不经常使用; 5-最近最少使用; 6-一致性hash;";

    public final byte type;

    @Getter
    public final String desc;

    LoadBalanceType(int type, String desc) {
        this((byte) type, desc);
    }

    LoadBalanceType(byte type, String desc) {
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
    public static LoadBalanceType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (LoadBalanceType loadBalanceType : values()) {
            if (type.byteValue() == loadBalanceType.type) {
                return loadBalanceType;
            }
        }

        return null;
    }

}
