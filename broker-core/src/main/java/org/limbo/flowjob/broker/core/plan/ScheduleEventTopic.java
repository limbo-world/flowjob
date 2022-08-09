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

package org.limbo.flowjob.broker.core.plan;

import org.limbo.flowjob.broker.core.events.EventTopic;

/**
 * @author Devil
 * @since 2022/8/5
 */
public enum ScheduleEventTopic implements EventTopic {
    /**
     * plan 开始执行
     */
    PLAN_EXECUTING,
    /**
     * plan 下发后续任务
     */
    PLAN_DISPATCH_NEXT,
    /**
     * plan 执行成功
     */
    PLAN_SUCCESS,
    /**
     * plan 执行失败
     */
    PLAN_FAIL,
    /**
     * job 开始执行
     */
    JOB_EXECUTING,
    /**
     * job 执行成功
     */
    JOB_SUCCESS,
    /**
     * job 执行失败
     */
    JOB_FAIL,
    /**
     * task 开始执行
     */
    TASK_EXECUTING,
    /**
     * task 执行成功
     */
    TASK_SUCCESS,
    /**
     * task 执行失败
     */
    TASK_FAIL,
    ;

}
