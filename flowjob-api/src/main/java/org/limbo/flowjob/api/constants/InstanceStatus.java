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

package org.limbo.flowjob.api.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 计划调度状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum InstanceStatus {
    UNKNOWN(ConstantsPool.UNKNOWN, "未知"),
    SCHEDULING(ConstantsPool.INSTANCE_SCHEDULING, "调度中"), // 时间轮中，还未创建 job 实例
    DISPATCHING(ConstantsPool.INSTANCE_DISPATCHING, "下发中"), // 创建完 job 实例，等待 job 下发
    EXECUTING(ConstantsPool.INSTANCE_EXECUTING, "执行中"), // 第一个任务切换为执行中的时候
    SUCCEED(ConstantsPool.INSTANCE_EXECUTE_SUCCEED, "执行成功"), // 所有节点都执行成功
    FAILED(ConstantsPool.INSTANCE_EXECUTE_FAILED, "执行失败"), // 有一个节点执行失败，并触发plan的失败场景
    ;

    @JsonValue
    public final int status;

    public final String desc;

    @JsonCreator
    InstanceStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     *
     * @param status 待校验状态值
     */
    public boolean is(InstanceStatus status) {
        return equals(status);
    }

    /**
     * 校验是否是当前状态
     *
     * @param status 待校验状态值
     */
    public boolean is(Number status) {
        return status != null && status.intValue() == this.status;
    }

    /**
     * 解析上下文状态值
     */
    @JsonCreator
    public static InstanceStatus parse(Number status) {
        for (InstanceStatus statusEnum : values()) {
            if (statusEnum.is(status)) {
                return statusEnum;
            }
        }
        return UNKNOWN;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return this == SUCCEED || this == FAILED;
    }

}
