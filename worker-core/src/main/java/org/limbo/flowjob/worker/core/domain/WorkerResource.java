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

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
    private int availableQueueSize;

    private Float availableCpu;

    private OperatingSystemMXBean osmxb;

    private CentralProcessor processor;

    private WorkerResource() {
    }

    public static WorkerResource create(int queueSize) throws Exception {
        WorkerResource resource = new WorkerResource();
        resource.queueSize = queueSize;
        resource.availableQueueSize = queueSize;
        resource.osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        SystemInfo systemInfo = new SystemInfo();
        resource.processor = systemInfo.getHardware().getProcessor();
        // 计算一次cpu
        resource.calAvailableCpu();

        // 定时刷新 cpu 信息
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    resource.calAvailableCpu();
                } catch (InterruptedException e) {
                    log.error("calculate available cpu error", e);
                }
            }
        }, 1000, 1000);

        return resource;
    }

    public long getAvailableRAM() {
        return osmxb.getFreePhysicalMemorySize();
    }

    public float getAvailableCpu() {
        return availableCpu; // 可能会有并发问题
    }

    private void calAvailableCpu() throws InterruptedException {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // 睡眠1s
        TimeUnit.SECONDS.sleep(1);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
//        System.out.println("cpu核数:" + processor.getLogicalProcessorCount());
//        System.out.println("total cpu:" + totalCpu);

        synchronized (this) {
            availableCpu = (float) (cSys * 1.0 / totalCpu);
        }
    }

    public int getAvailableQueueSize() {
        return availableQueueSize;
    }

    public void resize(int queueSize) {
        if (this.queueSize == queueSize) {
            return;
        }
        this.availableQueueSize = this.availableQueueSize + (queueSize - this.queueSize);
        this.queueSize = queueSize;
    }
}
