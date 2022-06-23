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
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 任务调度状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum TaskScheduleStatus {

    SCHEDULING(1, "调度中"), // 任务刚创建，还在内存，未下发给worker

    /**
     * 任务已创建，已持久化，正在下发给worker
     */
    DISPATCHING(2, "下发中"),

    /**
     * 任务已经创建且持久化，下发给worker失败，
     */
    DISPATCH_FAILED(3, "下发失败"),

    /**
     * 任务已经创建且持久化，下发给worker成功，正在执行中
     */
    EXECUTING(4, "执行中"),

    /**
     * 任务已经创建且持久化，worker反馈已执行完成
     */
    COMPLETED(5, "处理完成"),
    ;

    @JsonValue
    public final byte status;

    public final String desc;

    @JsonCreator
    TaskScheduleStatus(int status, String desc) {
        this(((byte) status), desc);
    }

    TaskScheduleStatus(byte status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(TaskScheduleStatus status) {
        return equals(status);
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(Number status) {
        return status != null && status.byteValue() == this.status;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static TaskScheduleStatus parse(Number status) {
        if (status == null) {
            return null;
        }

        for (TaskScheduleStatus scheduleStatus : values()) {
            if (scheduleStatus.is(status)) {
                return scheduleStatus;
            }
        }

        return null;
    }

}
