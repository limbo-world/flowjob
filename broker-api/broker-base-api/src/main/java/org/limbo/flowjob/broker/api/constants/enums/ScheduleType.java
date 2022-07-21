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
 * 作业调度方式：
 * <ul>
 *     <li>{@linkplain ScheduleType#NONE 无需调度}</li>
 *     <li>{@linkplain ScheduleType#DELAYED 固定延迟}</li>
 *     <li>{@linkplain ScheduleType#FIXED_RATE 固定速度}</li>
 *     <li>{@linkplain ScheduleType#FIXED_INTERVAL 固定间隔时间}</li>
 *     <li>{@linkplain ScheduleType#CRON CRON}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum ScheduleType implements DescribableEnum<Byte> {

    /**
     * 无需调度 由其它方式触发 // todo 此类型需要检查有 api方式的最初节点 非此类型要检查有 schedule 的最初节点
     */
    NONE(0, "无需调度"),

    /**
     * 固定延迟，作业创建后，从创建时间起延迟一定时间后触发调度。只调度一次。
     */
    DELAYED(1, "固定延迟"),

    /**
     * 固定速度，作业创建后，从创建时间起延迟一定时间后触发作业调度。每次调度下发成功后，间隔固定时间长度后，再次触发作业调度。
     */
    FIXED_RATE(2, "固定速度"),

    /**
     * 固定间隔，作业创建后，从创建时间起延迟一定时间后触发作业调度。每次作业下发执行完成（成功或失败）后，间隔固定时间长度后，再次触发作业调度。
     */
    FIXED_INTERVAL(3, "固定间隔时间"),

    /**
     * 通过CRON表达式指定作业触发调度的时间点。FIXED_RATE 的另一种模式
     */
    CRON(4, "CRON表达式"),

    ;

    public static final String DESCRIPTION = "1-固定延迟; 2-固定速度; 3-固定间隔时间; 4-CRON表达式;";

    public final byte type;

    @Getter
    public final String desc;


    ScheduleType(int type, String desc) {
        this(((byte) type), desc);
    }

    ScheduleType(byte type, String desc) {
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
     * 解析作业调度类型，用于Jackson反序列化
     * @param type 数值类型的作业调度类型
     * @return 作业调度类型枚举
     */
    @JsonCreator
    public static ScheduleType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (ScheduleType scheduleType : values()) {
            if (type.byteValue() == scheduleType.type) {
                return scheduleType;
            }
        }

        return null;
    }

}
