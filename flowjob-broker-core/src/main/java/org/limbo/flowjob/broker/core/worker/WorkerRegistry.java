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

package org.limbo.flowjob.broker.core.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.constants.WorkerConstant;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeFormateUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Devil
 * @since 2023/11/21
 */
@Slf4j
public class WorkerRegistry {

    private static final Map<String, Worker> RUNNING_WORKER_MAP = new ConcurrentHashMap<>();

    /**
     * 心跳超时时间，毫秒
     */
    private final Duration heartbeatTimeout = Duration.ofSeconds(WorkerConstant.HEARTBEAT_TIMEOUT_SECOND);

    private final WorkerRepository workerRepository;

    public WorkerRegistry(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public void init() {
        new Timer().schedule(new WorkerOnlineCheckTask(), 0, heartbeatTimeout.toMillis());
        new Timer().schedule(new WorkerFusingCheckTask(), 0, heartbeatTimeout.toMillis());
        new Timer().schedule(new WorkerTerminatedCheckTask(), 0, heartbeatTimeout.toMillis());
    }

    public Collection<Worker> all() {
        return RUNNING_WORKER_MAP.values();
    }

    private class WorkerOnlineCheckTask extends TimerTask {

        private static final String TASK_NAME = "[WorkerOnlineCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime();
                if (log.isDebugEnabled()) {
                    log.info("{} checkOnline start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<Worker> workers = workerRepository.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(workers)) {
                    for (Worker worker : workers) {
                        URL url = worker.getUrl();
                        WorkerMetric metric = worker.getMetric();
                        Worker n = RUNNING_WORKER_MAP.put(worker.getId(), worker);
                        if (n == null && log.isDebugEnabled()) {
                            log.debug("{} find online id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, worker.getId(), url.getHost(), url.getPort(), TimeFormateUtils.format(metric.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }

    }

    private class WorkerFusingCheckTask extends TimerTask {

        private static final String TASK_NAME = "[WorkerFusingCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime().plusSeconds(-heartbeatTimeout.getSeconds());
                if (log.isDebugEnabled()) {
                    log.debug("{} check start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<Worker> workers = workerRepository.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(workers)) {
                    for (Worker worker : workers) {
                        WorkerMetric metric = worker.getMetric();
                        if (log.isDebugEnabled()) {
                            log.debug("{} find id: {} lastHeartbeat:{}", TASK_NAME, worker.getId(), TimeFormateUtils.format(metric.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                        RUNNING_WORKER_MAP.remove(worker.getId());
                        // 更新状态
                        if (WorkerStatus.RUNNING == worker.getStatus()) {
                            workerRepository.updateStatus(worker.getId(), WorkerStatus.RUNNING.status, WorkerStatus.FUSING.status);
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }
    }

    private class WorkerTerminatedCheckTask extends TimerTask {

        private static final String TASK_NAME = "[WorkerTerminatedCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-3 * heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * heartbeatTimeout.getSeconds());
                if (log.isDebugEnabled()) {
                    log.debug("{} check start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<Worker> workers = workerRepository.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(workers)) {
                    for (Worker worker : workers) {
                        WorkerMetric metric = worker.getMetric();
                        if (log.isDebugEnabled()) {
                            log.debug("{} find id: {} lastHeartbeat:{}", TASK_NAME, worker.getId(), TimeFormateUtils.format(metric.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                        RUNNING_WORKER_MAP.remove(worker.getId());
                        // 更新状态
                        if (WorkerStatus.FUSING == worker.getStatus()) {
                            workerRepository.updateStatus(worker.getId(), WorkerStatus.FUSING.status, WorkerStatus.TERMINATED.status);
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }
    }

}
