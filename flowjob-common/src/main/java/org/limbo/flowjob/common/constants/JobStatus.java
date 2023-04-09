/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 作业调度状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
@Getter
public enum JobStatus {

    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),
    SCHEDULING(ConstantsPool.SCHEDULE_STATUS_SCHEDULING, "调度中"),
    EXECUTING(ConstantsPool.SCHEDULE_STATUS_EXECUTING, "执行中"),
    SUCCEED(ConstantsPool.SCHEDULE_STATUS_EXECUTE_SUCCEED, "执行成功"),
    FAILED(ConstantsPool.SCHEDULE_STATUS_EXECUTE_FAILED, "执行异常"), // worker拒绝，进入容错策略 失败次数不增加 TERMINATED 作业被手动终止 不再增加一个状态 而是写入 errMsg
    ;



    @JsonValue
    public final byte status;

    public final String desc;

    @JsonCreator
    JobStatus(int status, String desc) {
        this(((byte) status), desc);
    }

    JobStatus(byte status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     *
     * @param status 待校验状态值
     */
    public boolean is(JobStatus status) {
        return equals(status);
    }

    /**
     * 校验是否是当前状态
     *
     * @param status 待校验状态值
     */
    public boolean is(Number status) {
        return status != null && status.byteValue() == this.status;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static JobStatus parse(Number status) {
        if (status == null) {
            return UNKNOWN;
        }

        for (JobStatus statusEnum : values()) {
            if (statusEnum.is(status)) {
                return statusEnum;
            }
        }

        return UNKNOWN;
    }

}
