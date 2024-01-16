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

package org.limbo.flowjob.agent.core.checker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.common.utils.time.LocalDateTimeUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 处理一直下发中的task
 *
 * @author Devil
 * @since 2023/8/15
 */
@Slf4j
public class TaskScheduleChecker {

    private Timer timer;

    private TimerTask task;

    private Duration period;

    /**
     * 是否运行中
     */
    private boolean running;

    private TaskRepository taskRepository;

    private TaskDispatcher taskDispatcher;

    private static final String CHECKER_NAME = "TaskDispatchChecker";

    public TaskScheduleChecker(TaskRepository taskRepository, TaskDispatcher taskDispatcher, Duration period) {
        this.taskRepository = taskRepository;
        this.taskDispatcher = taskDispatcher;
        this.timer = new Timer(CHECKER_NAME);
        this.period = period;
        this.running = false;
    }

    public synchronized void start() {
        if (this.running) {
            return;
        }

        // 已经有任务存在，停止
        if (this.task != null) {
            this.task.cancel();
        }

        this.task = new TimerTask() {

            @Override
            public void run() {
                try {
                    Integer limit = 100;
                    String startId = "";

                    String triggerAt = LocalDateTimeUtils.formatYMDHMS(TimeUtils.currentLocalDateTime().plusSeconds(-5));

                    List<Task> tasks = taskRepository.getUnScheduled(triggerAt, startId, limit);
                    while (CollectionUtils.isNotEmpty(tasks)) {
                        for (Task t : tasks) {
                            taskDispatcher.dispatch(t);
                        }
                        startId = tasks.get(tasks.size() - 1).getId();
                        tasks = taskRepository.getUnScheduled(triggerAt, startId, limit);
                    }
                } catch (Exception e) {
                    log.error("[{}] error", CHECKER_NAME, e);
                }
            }
        };

        this.timer.schedule(this.task, 10, this.period.toMillis());
        this.running = true;

        log.info("[{}] start!", CHECKER_NAME);
    }


    public synchronized void stop() {
        if (!this.running) {
            return;
        }

        TimerTask prevTask = this.task;
        this.task = null;

        if (prevTask != null) {
            prevTask.cancel();
        }

        this.running = false;

        log.info("[{}] stop!", CHECKER_NAME);
    }


}
