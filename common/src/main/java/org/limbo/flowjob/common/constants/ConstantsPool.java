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

/**
 * @author Devil
 * @since 2022/12/3
 */
public interface ConstantsPool {

    byte STATUS_UNKNOWN = 0;

    // PLanStatus
    byte PLAN_STATUS_SCHEDULING = 1;
    byte PLAN_STATUS_EXECUTING = 2;
    byte PLAN_STATUS_SUCCEED = 3;
    byte PLAN_STATUS_FAILED = 4;

    // JobStatus
    byte JOB_STATUS_SCHEDULING = 1;
    byte JOB_STATUS_EXECUTING = 2;
    byte JOB_STATUS_SUCCEED = 3;
    byte JOB_STATUS_FAILED = 4;

    // TaskStatus
    byte TASK_STATUS_SCHEDULING = 1;
    byte TASK_STATUS_DISPATCHING = 2;
    byte TASK_STATUS_EXECUTING = 3;
    byte TASK_STATUS_SUCCEED = 4;
    byte TASK_STATUS_FAILED = 5;
}
