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
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 作业调度方式：
 * <ul>
 *     <li>{@linkplain JobScheduleType#DELAYED 固定延迟}</li>
 *     <li>{@linkplain JobScheduleType#FIXED_RATE 固定速度}</li>
 *     <li>{@linkplain JobScheduleType#FIXED_INTERVAL 固定间隔时间}</li>
 *     <li>{@linkplain JobScheduleType#CRON CRON}</li>
 *     <li>{@linkplain JobScheduleType#DAG DAG工作流}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum JobScheduleType {

    /**
     * 固定延迟，作业创建后，从创建时间起延迟一定时间后触发调度。只调度一次。
     */
    DELAYED(1, "固定延迟"),

    /**
     * 固定速度，作业创建后，从创建时间起延迟一定时间后触发作业调度。每次调度下发成功后，间隔固定时间长度后，再次触发作业调度。
     */
    FIXED_RATE(2, "固定速度"),

    /**
     * 固定速度，作业创建后，从创建时间起延迟一定时间后触发作业调度。每次作业下发执行完成（成功或失败）后，间隔固定时间长度后，再次触发作业调度。
     */
    FIXED_INTERVAL(3, "固定间隔时间"),

    /**
     * 通过CRON表达式指定作业触发调度的时间点。
     */
    CRON(4, "CRON表达式"),

    /**
     * TODO
     */
    DAG(5, "DAG工作流"),

    ;

    @JsonValue
    public final int type;

    public final String desc;


    JobScheduleType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 解析作业调度类型，用于Jackson反序列化
     * @param type 数值类型的作业调度类型
     * @return 作业调度类型枚举
     */
    @JsonCreator
    public static JobScheduleType parse(Integer type) {
        if (type == null) {
            return null;
        }

        for (JobScheduleType jobScheduleType : values()) {
            if (type.equals(jobScheduleType.type)) {
                return jobScheduleType;
            }
        }

        return null;
    }

}
