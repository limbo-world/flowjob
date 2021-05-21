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

package org.limbo.flowjob.tracker.core.job;

import lombok.Getter;

/**
 * 作业上下文状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum JobContextStatus implements JobContext.Status {

    INIT(1, "初始化"),

    DISPATCHING(2, "分发中"),
    REFUSED(3, "拒绝执行"),
    EXECUTING(3, "执行中"),

    SUCCEED(4, "执行成功"),
    FAILED(5, "执行异常"),
    TERMINATED(6, "作业被手动终止"),
    ;

    @Getter
    public final int status;

    @Getter
    public final String desc;

    JobContextStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}
