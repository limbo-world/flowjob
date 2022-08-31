/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.worker.core.executor.TaskRepository;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * worker自身资源情况
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class WorkerResource {

    /**
     * 可分配任务总数
     */
    private int queueSize;

    /**
     * 剩余可分配任务数
     */
    @Getter
    private int availableQueueSize;

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
    private WorkerResourcesCalculator calculator;

    /**
     * 定时计算任务
     */
    private TimerTask calculateTask;

    /**
     * 任务仓库
     */
    private TaskRepository taskRepository;

    private WorkerResource() {
    }


    /**
     * 静态工厂
     *
     * @param queueSize 初始化任务队列大小
     */
    public static WorkerResource create(int queueSize, TaskRepository taskRepository) {
        WorkerResource resource = new WorkerResource();
        resource.queueSize = queueSize;
        resource.availableQueueSize = queueSize;
        resource.calculator = new WorkerResourcesCalculator();
        resource.availableCpu = resource.calculator.calculateAvailableCpu(Duration.ofSeconds(1));
        resource.taskRepository = taskRepository;
        resource.startCalculateTask();
        return resource;
    }


    /**
     * 启动 Worker 资源定时检测任务
     */
    private void startCalculateTask() {
        // 定时刷新 cpu 信息
        if (this.calculateTask != null) {
            this.calculateTask.cancel();
        }

        this.calculateTask = new TimerTask() {
            @Override
            public void run() {
                availableCpu = calculator.calculateAvailableCpu(Duration.ofSeconds(1));
                availableRam = calculator.calculateAvailableRam();
                availableQueueSize = queueSize - taskRepository.count();
            }
        };

        // 启动任务
        Timer calculateScheduler = new Timer("WorkerResourceCalculateTimer");
        calculateScheduler.schedule(this.calculateTask, 1000, 1000);
    }


    /**
     * 重新调整任务队列大小
     */
    public void resize(int queueSize) {
        if (this.queueSize == queueSize) {
            return;
        }

        this.availableQueueSize = this.availableQueueSize + (queueSize - this.queueSize);
        this.queueSize = queueSize;
    }

}
