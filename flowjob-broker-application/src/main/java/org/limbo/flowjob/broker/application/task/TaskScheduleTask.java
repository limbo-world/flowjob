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

package org.limbo.flowjob.broker.application.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.application.schedule.ScheduleStrategy;
import org.limbo.flowjob.broker.application.support.CommonThreadPool;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;

import java.time.LocalDateTime;

/**
 * 对Task进行处理，下发
 *
 * @author pengqi
 * @date 2023/1/9
 */
@Slf4j
public class TaskScheduleTask implements MetaTask {

    @Getter
    private final Task task;

    /**
     * 期望的触发时间
     */
    @Getter
    private final LocalDateTime triggerAt;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final ScheduleStrategy scheduleStrategy;

    public TaskScheduleTask(Task task, ScheduleStrategy scheduleStrategy) {
        this.task = task;
        this.triggerAt = task.getTriggerAt();
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void execute() {
        CommonThreadPool.IO.submit(() -> {
            try {
                scheduleStrategy.schedule(task);
            } catch (Exception e) {
                log.error("task {} schedule fail", task.getTaskId(), e);
            }
        });
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.TASK;
    }

    @Override
    public String getMetaId() {
        return task.getTaskId();
    }

    @Override
    public LocalDateTime scheduleAt() {
        return triggerAt;
    }
}
