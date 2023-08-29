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

package org.limbo.flowjob.agent.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.core.service.TaskService;
import org.limbo.flowjob.common.utils.time.DateTimeUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Devil
 * @since 2023/8/15
 */
@Slf4j
public class TaskChecker {

    private Timer timer;

    private TimerTask task;

    private Duration period;

    /**
     * 是否运行中
     */
    private boolean running;

    private TaskService taskService;

    /**
     * 上次检测时间
     */
    private LocalDateTime lastCheckTime;

    public TaskChecker(TaskService taskService, Duration period) {
        this.taskService = taskService;
        this.timer = new Timer("TaskChecker");
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

        this.lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * period.getSeconds());

        this.task = new TimerTask() {

            @Override
            public void run() {
                try {
                    LocalDateTime curCheckTime = TimeUtils.currentLocalDateTime().plus(-period.toMillis(), ChronoUnit.MILLIS);
                    String startId = "";
                    Integer limit = 1000;
                    List<Task> tasks = taskService.getByLastReportBetweenAndUnFinish(DateTimeUtils.formatYMDHMS(lastCheckTime), DateTimeUtils.formatYMDHMS(curCheckTime), startId, limit);
                    while (CollectionUtils.isNotEmpty(tasks)) {
                        for (Task t : tasks) {
                            if (t.getWorker() != null) {
                                taskService.taskFail(t, String.format("worker %s is offline", t.getWorker().getId()), "");
                            } else {
                                taskService.taskFail(t, "no worker", "");
                            }
                        }
                        startId = tasks.get(tasks.size() - 1).getTaskId();
                        tasks = taskService.getByLastReportBetweenAndUnFinish(DateTimeUtils.formatYMDHMS(lastCheckTime), DateTimeUtils.formatYMDHMS(curCheckTime), startId, limit);
                    }
                } catch (Exception e) {
                    log.error("[TaskChecker] error", e);
                }
            }
        };

        this.timer.schedule(this.task, 10, this.period.toMillis());
        this.running = true;
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
    }


}
