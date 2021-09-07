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
 * 调度状态
 * @author Brozen
 * @since 2021-05-19
 */
public enum TaskScheduleStatus {

    SCHEDULING(1, "调度中"), // 任务刚创建，还在内存，未下发给worker
    EXECUTING(2, "执行中"), // worker接收任务成功
    FEEDBACK(3, "执行完成"), // worker反馈成功，但是具体对Plan等异步状态变更还没处理
    COMPLETED(4, "处理完成"), // 整体逻辑执行结束
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
