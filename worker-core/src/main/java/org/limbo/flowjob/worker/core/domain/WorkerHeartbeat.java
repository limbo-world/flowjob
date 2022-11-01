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

package org.limbo.flowjob.worker.core.domain;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Brozen
 * @since 2022-08-30
 */
public class WorkerHeartbeat {

    /**
     * 工作节点
     */
    private Worker worker;

    /**
     * 心跳任务调度器
     */
    private final Timer heartbeatScheduler;

    /**
     * Worker 心跳发送定时任务
     */
    private TimerTask heartbeatTask;

    /**
     * 心跳周期
     */
    private final Duration period;

    /**
     * 是否运行中
     */
    private boolean beating;


    public WorkerHeartbeat(Worker worker, Duration period) {
        this.worker = worker;
        this.heartbeatScheduler = new Timer("WorkerHeartbeat-" + worker.getName());
        this.period = period;
        this.beating = false;
    }


    /**
     * 启动心跳
     */
    public synchronized void start() {
        if (this.beating) {
            return;
        }

        // 已经有任务存在，停止
        if (this.heartbeatTask != null) {
            this.heartbeatTask.cancel();
        }

        // 重新生成心跳任务
        this.heartbeatTask = new TimerTask() {
            @Override
            public void run() {
                // So beat it
                worker.sendHeartbeat();
            }
        };

        // 启动心跳任务
        this.heartbeatScheduler.schedule(this.heartbeatTask, 0, this.period.toMillis());
        this.beating = true;
    }


    /**
     * 停止心跳任务
     */
    public synchronized void stop() {
        if (!this.beating) {
            return;
        }

        TimerTask prevTask = this.heartbeatTask;
        this.heartbeatTask = null;

        if (prevTask != null) {
            prevTask.cancel();
        }

        this.beating = false;
    }

}
