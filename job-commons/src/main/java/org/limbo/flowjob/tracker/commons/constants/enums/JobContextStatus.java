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
 * 作业上下文状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum JobContextStatus {

    INIT(1, "初始化"),

    DISPATCHING(3, "分发中"),
    REFUSED(4, "拒绝执行"),
    EXECUTING(5, "执行中"),

    // DAG作业状态
    PART_EXECUTING(12, "部分执行中"),

    SUCCEED(6, "执行成功"),
    FAILED(7, "执行异常"),
    TERMINATED(8, "作业被手动终止"),
    ;

    @JsonValue
    public final byte status;

    public final String desc;

    @JsonCreator
    JobContextStatus(int status, String desc) {
        this(((byte) status), desc);
    }

    JobContextStatus(byte status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(JobContextStatus status) {
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
    public static JobContextStatus parse(Number status) {
        if (status == null) {
            return null;
        }

        for (JobContextStatus jobContextStatus : values()) {
            if (jobContextStatus.is(status)) {
                return jobContextStatus;
            }
        }

        return null;
    }

}
