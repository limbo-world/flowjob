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
 * DAG作业上下文状态
 *
 * @author Brozen
 * @since 2021-05-19
 */
public enum DAGJobContextStatus implements JobContext.Status {

    PART_EXECUTING(12, "部分执行中"),

    ;

    @Getter
    public final int status;

    @Getter
    public final String desc;

    DAGJobContextStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}
