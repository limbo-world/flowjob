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

/**
 * 作业调度方式
 *
 * @author Brozen
 * @since 2021-05-16
 */
public enum JobScheduleType {

    DELAY(1, "固定延迟"),

    FIXED_RATE(2, "固定速度"),

    FIXED_INTERVAL(3, "固定间隔时间"),

    CORN(4, "CORN表达式"),

    DAG(5, "DAG工作流"),

    ;


    public final int type;

    public final String desc;

    JobScheduleType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
