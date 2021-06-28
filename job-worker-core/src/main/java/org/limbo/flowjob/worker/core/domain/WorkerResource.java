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
 * @author Devil
 * @date 2021/6/23 3:46 下午
 */
@Slf4j
public class WorkerResource {

    private int queueSize;

    private Double availableCpu;

    private int availableQueueSize;

    private OperatingSystemMXBean osmxb;

    private SystemInfo systemInfo;

    private WorkerResource() {
    }

    public static WorkerResource create(int queueSize) {
        WorkerResource resource = new WorkerResource();
        resource.queueSize = queueSize;
        resource.osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        resource.systemInfo = new SystemInfo();

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

    public double getAvailableCpu() {
        synchronized (this) {
            if (availableCpu == null) {
                try {
                    calAvailableCpu();
                } catch (InterruptedException e) {
                    log.error("calculate available cpu error", e);
                }
                return availableCpu; // 可能会有并发问题
            } else {
                return availableCpu;
            }
        }
    }

    private void calAvailableCpu() throws InterruptedException {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
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
            availableCpu = cSys * 1.0 / totalCpu;
        }
    }

    public int getAvailableQueueSize() {
        return availableQueueSize;
    }

    public void resize(int queueSize) {
        this.queueSize = queueSize;
    }
}
