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

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.LongStream;

/**
 * @author Brozen
 * @since 2022-08-30
 */
@Slf4j
public class WorkerResourcesCalculator {

    private final OperatingSystemMXBean osMxBean;

    private final CentralProcessor processor;


    public WorkerResourcesCalculator() {
        this.osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.processor = new SystemInfo().getHardware().getProcessor();
    }


    /**
     * 计算可用的 CPU 核数。
     * 通过截取两个时间点的 CPU tick 数据，计算出两个时间点之间的 idle tick 数量，以及两个时间点之间的总 CPU tick 数量，
     * 即可计算出这段时间内 CPU 的空闲比率，空闲比率乘 CPU 核数，作为可用 CPU 核数来返回。
     *
     * @param sampleDuration CPU tick 采样时长，
     * @return 可用的 CPU 处理器数量。
     */
    public float getAvailableCpu(Duration sampleDuration) {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        LockSupport.parkNanos(sampleDuration.toNanos());
        long[] currTicks = processor.getSystemCpuLoadTicks();

        // 空闲 tick 数量
        long idle = subtract(currTicks, prevTicks, CentralProcessor.TickType.IDLE);
        // 总 tick 数量
        long totalCpu = LongStream.of(currTicks).sum() - LongStream.of(prevTicks).sum();

        if (log.isDebugEnabled()) {
            log.debug("System CPUs: {}", processor.getLogicalProcessorCount());
            log.debug("System CPU total: {}", totalCpu);
            log.debug("System CPU used: {}", new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
        }

        return (float) (idle * 1.0 / totalCpu) * processor.getLogicalProcessorCount();
    }


    /**
     * 计算两个时间点的 tick 中，某种类型的 tick 数量。
     */
    private long subtract(long[] currTick, long[] prevTick, CentralProcessor.TickType type) {
        int idx = type.getIndex();
        if (idx >= currTick.length || idx >= prevTick.length) {
            return 0;
        }

        return currTick[idx] - prevTick[idx];
    }


    /**
     * 计算可用的 RAM，单位字节
     */
    public long getAvailableRam() {
        return osMxBean.getFreePhysicalMemorySize();
    }

}
