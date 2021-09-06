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

/**
 * 作业调度状态
 * @author Brozen
 * @since 2021-05-19
 */
public interface TaskScheduleStatus {
    /**
     * 调度中 任务刚创建，还在内存，未下发给worker
     */
    byte Scheduling = 1;
    /**
     * 执行中 worker接收任务成功
     */
    byte EXECUTING = 2;
    /**
     * 执行完成 worker反馈成功，但是具体对Plan等异步状态变更还没处理
     */
    byte FEEDBACK = 3;
    /**
     * 处理完成 整体逻辑执行结束
     */
    byte COMPLETED = 4;

}
