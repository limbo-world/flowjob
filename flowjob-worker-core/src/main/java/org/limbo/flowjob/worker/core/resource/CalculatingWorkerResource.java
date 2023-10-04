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

package org.limbo.flowjob.worker.core.resource;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 动态计算的 worker 资源
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
@Accessors(fluent = true)
public class CalculatingWorkerResource extends AbstractWorkerResources {

    /**
     * 可用 CPU 核数
     */
    @Getter
    private volatile float availableCpu;

    /**
     * 可用的 RAM 内存数量
     */
    @Getter
    private volatile long availableRam;

    /**
     * Worker 资源计算器
     */
    private final WorkerResourcesCalculator calculator;

    /**
     * 定时计算任务
     */
    private TimerTask calculateTask;


    public CalculatingWorkerResource(int concurrency, int queueSize) {
        super(concurrency, queueSize);
        this.calculator = new WorkerResourcesCalculator();
        this.availableCpu = this.calculator.getAvailableCpu(Duration.ofSeconds(1));
        this.availableRam = this.calculator.getAvailableRam();

        startCalculateTask();
    }


    /**
     * 启动 Worker 资源定时检测任务
     */
    private void startCalculateTask() {
        // 定时刷新 cpu 信息
        if (this.calculateTask != null) {
            this.calculateTask.cancel();
        }

        // 创建新的定时任务
        this.calculateTask = new TimerTask() {
            @Override
            public void run() {
                availableCpu = calculator.getAvailableCpu(Duration.ofSeconds(1));
                availableRam = calculator.getAvailableRam();
            }
        };

        // 启动任务
        Timer calculateScheduler = new Timer("WorkerResourceCalculateTimer");
        calculateScheduler.schedule(this.calculateTask, 1000, 1000);
    }

}
