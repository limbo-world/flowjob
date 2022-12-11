/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.Getter;
import org.limbo.flowjob.broker.core.schedule.Scheduled;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2022-10-11
 */
public abstract class MetaTask implements Scheduled {

    @Getter
    private final String taskId;

    /**
     * 下次任务触发时间
     */
    private LocalDateTime triggerAt;


    protected MetaTask(String taskId) {
        this.taskId = taskId;
    }


    /**
     * 触发元任务执行，并更新元任务的触发时间。
     */
    public void execute() {
        executeTask();
        nextTriggerTime(false);
    }


    /**
     *
     */
    protected abstract void executeTask();

    protected void nextTriggerTime(boolean firstTime) {
        this.triggerAt = calculateNextTriggerTime(firstTime);
    }

    /**
     * 计算当前元任务的下次触发时间。
     * @param firstTime 是否计算首次触发间隔
     */
    protected abstract LocalDateTime calculateNextTriggerTime(boolean firstTime);


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String scheduleId() {
        return this.taskId;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public LocalDateTime triggerAt() {
        return this.triggerAt;
    }


}
