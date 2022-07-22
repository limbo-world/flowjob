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

package org.limbo.flowjob.broker.core.schedule;

import java.time.LocalDateTime;

/**
 * 待调度对象接口
 *
 * @author Brozen
 * @since 2021-07-12
 */
public interface Schedulable {

    /**
     * 获取调度对象ID
     */
    String scheduleId();

    /**
     * 获取调度配置
     */
    ScheduleOption scheduleOption();

    /**
     * 获取上次被调度时间
     */
    LocalDateTime scheduleAt();

    /**
     * 获取上次调度反馈的时间
     */
    LocalDateTime feedbackAt();

    /**
     * 执行业务
     */
    void schedule();

    /**
     * 计算作业下一次被触发时的时间戳
     */
    default long triggerAt() {
        throw new UnsupportedOperationException();
    }

}
