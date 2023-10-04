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

package org.limbo.flowjob.broker.core.schedule.selector;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.common.utils.concurrent.Lockable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单机内存存储 Worker 统计数据
 *
 * @author Brozen
 * @since 2022-12-21
 */
public class SingletonWorkerStatisticsRepo extends Lockable<List<SingletonWorkerStatisticsRepo.WorkerDispatchRecord>> implements WorkerStatisticsRepository {

    /**
     * 最久统计多长时间的数据，默认 12H。
     */
    @Setter
    private Duration maxStatisticDuration = Duration.ofHours(12);


    public SingletonWorkerStatisticsRepo() {
        super(new ArrayList<>());
    }


    /**
     * 记录任务被下发
     */
    @Override
    public void recordDispatched(Worker worker) {
        runInWriteLock(records -> {
            // 新增记录
            records.add(new WorkerDispatchRecord(
                    worker.getId(), Instant.now()
            ));

            // 同时检测头部 10 条记录是否过时，如过时需要移除
            int maxDetectItems = 10;
            Instant expiresLimit = Instant.now().plusSeconds(-maxStatisticDuration.getSeconds());
            Iterator<WorkerDispatchRecord> iterator = records.iterator();
            for (int i = 0; i < maxDetectItems; i++) {
                if (!iterator.hasNext()) {
                    break;
                }

                WorkerDispatchRecord r = iterator.next();
                if (r.dispatchAt.isBefore(expiresLimit)) {
                    iterator.remove();
                } else {
                    break;
                }
            }
        });
    }


    /**
     * {@inheritDoc}
     * @param workerIds Worker ID 集合
     * @param limit 查询时间点
     * @return
     */
    @Override
    public List<WorkerLBStatistics> list(Set<String> workerIds, Instant limit) {
        // 读取所有统计数据
        Map<String, MutableWorkerLBStatistics> statistics = new HashMap<>();
        runInReadLock(records -> {
            for (int i = records.size() - 1; i >= 0; i--) {
                WorkerDispatchRecord dispatchRecord = records.get(i);
                if (workerIds.contains(dispatchRecord.workerId) && dispatchRecord.dispatchAt.isAfter(limit)) {
                    MutableWorkerLBStatistics s = statistics.computeIfAbsent(
                            dispatchRecord.workerId,
                            MutableWorkerLBStatistics::new
                    );
                    s.statisticRecord(dispatchRecord);
                }
            }

            for (String workerId : workerIds) {
                statistics.computeIfAbsent(
                        workerId,
                        MutableWorkerLBStatistics::new
                );
            }

        });

        return statistics.values().stream()
                .map(s -> new WorkerLBStatistics(s.workerId, s.lastDispatchTaskAt, s.dispatchTimes))
                .collect(Collectors.toList());
    }



    public static class MutableWorkerLBStatistics {
        private final String workerId;
        private Instant lastDispatchTaskAt;
        private int dispatchTimes;

        MutableWorkerLBStatistics(String workerId) {
            this.workerId = workerId;
        }

        void statisticRecord(WorkerDispatchRecord dispatchRecord) {
            if (this.lastDispatchTaskAt == null || this.lastDispatchTaskAt.isBefore(dispatchRecord.dispatchAt)) {
                this.lastDispatchTaskAt = dispatchRecord.dispatchAt;
            }

            this.dispatchTimes++;
        }

    }



    @AllArgsConstructor
    public static class WorkerDispatchRecord {

        public final String workerId;

        /**
         * 下发时间点
         */
        public final Instant dispatchAt;

    }

}
