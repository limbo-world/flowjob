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
 * 任务节点触发方式
 * <ul>
 *     <li>{@linkplain JobTriggerType#UNKNOWN 未知}</li>
 *     <li>{@linkplain JobTriggerType#OUTSIDE 外部触发}</li>
 *     <li>{@linkplain JobTriggerType#SCHEDULE 调度触发}</li>
 *     <li>{@linkplain JobTriggerType#PRE_FINISH 前置节点完成触发}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum JobTriggerType implements DescribableEnum<Byte> {

    /**
     * 外部触发 如console worker 等
     */
    UNKNOWN(0, "未知"),
    /**
     * 外部触发 如console worker 等
     */
    OUTSIDE(1, "外部触发"),

    /**
     * 任务调度后触发
     */
    SCHEDULE(2, "调度触发"),

    /**
     * 上个节点执行结束后触发
     */
    PRE_FINISH(3, "前置节点完成触发"),
    ;

    /**
     * 可通过{@link #describe(Class)}方法生成，用于在swagger3为枚举添加说明
     */
    public static final String DESCRIPTION = "0-未知; 1-外部触发; 2-调度触发; 3-前置节点完成触发;";

    public final byte type;

    @Getter
    public final String desc;

    JobTriggerType(int type, String desc) {
        this((byte) type, desc);
    }

    JobTriggerType(byte type, String desc) {
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
    public static JobTriggerType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (JobTriggerType loadBalanceType : values()) {
            if (type.byteValue() == loadBalanceType.type) {
                return loadBalanceType;
            }
        }

        return null;
    }

}
