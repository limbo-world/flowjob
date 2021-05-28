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

import lombok.Getter;
import org.limbo.flowjob.tracker.commons.beans.domain.job.JobContext;

/**
 * 作业上下文状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum JobContextStatus implements JobContext.Status {

    INIT(1, "初始化"),

    SCHEDULING(2, "调度中"),
    DISPATCHING(3, "分发中"),
    REFUSED(4, "拒绝执行"),
    EXECUTING(5, "执行中"),

    SUCCEED(6, "执行成功"),
    FAILED(7, "执行异常"),
    TERMINATED(8, "作业被手动终止"),
    ;

    @Getter
    public final int status;

    @Getter
    public final String desc;

    JobContextStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(JobContext.Status status) {
        return equals(status);
    }

    /**
     * 校验是否是当前状态
     * @param status 待校验状态值
     */
    public boolean is(Integer status) {
        return status != null && status.equals(this.status);
    }

}
