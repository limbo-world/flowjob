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

package org.limbo.flowjob.broker.application.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerMetricEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerMetricEntityRepo;
import org.limbo.flowjob.common.constants.WorkerConstant;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeFormateUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
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
 * @since 2023/9/12
 */
@Slf4j
@Component
public class WorkerRegistry implements InitializingBean {

    private static final Map<String, WorkerEntity> map = new ConcurrentHashMap<>();

    @Setter(onMethod_ = @Inject)
    private WorkerEntityRepo workerEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerMetricEntityRepo workerMetricEntityRepo;

    /**
     * 心跳超时时间，毫秒
     */
    private Duration heartbeatTimeout = Duration.ofSeconds(WorkerConstant.HEARTBEAT_TIMEOUT_SECOND);

    @Override
    public void afterPropertiesSet() throws Exception {
        new Timer().schedule(new WorkerOnlineCheckTask(), 0, heartbeatTimeout.toMillis());
        new Timer().schedule(new WorkerOfflineCheckTask(), 0, heartbeatTimeout.toMillis());
    }

    public Collection<WorkerEntity> all() {
        return map.values();
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
                List<WorkerMetricEntity> onlines = workerMetricEntityRepo.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(onlines)) {
                    for (WorkerMetricEntity online : onlines) {
                        WorkerEntity workerEntity = workerEntityRepo.findByWorkerIdAndDeleted(online.getWorkerId(), false).orElse(null);
                        if (workerEntity == null) {
                            map.remove(online.getWorkerId());
                            if (log.isDebugEnabled()) {
                                log.debug("{} find online but is null id: {}, lastHeartbeat:{}", TASK_NAME, online.getWorkerId(), TimeFormateUtils.format(online.getLastHeartbeatAt(), Formatters.YMD_HMS));
                            }
                        } else {
                            WorkerEntity n = map.putIfAbsent(workerEntity.getWorkerId(), workerEntity);
                            if (n == null && log.isDebugEnabled()) {
                                log.debug("{} find online id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, workerEntity.getWorkerId(), workerEntity.getHost(), workerEntity.getPort(), TimeFormateUtils.format(online.getLastHeartbeatAt(), Formatters.YMD_HMS));
                            }
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }

    }

    private class WorkerOfflineCheckTask extends TimerTask {

        private static final String TASK_NAME = "[WorkerOfflineCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime().plusSeconds(-heartbeatTimeout.getSeconds());
                if (log.isDebugEnabled()) {
                    log.debug("{} checkOffline start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<WorkerMetricEntity> offlines = workerMetricEntityRepo.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(offlines)) {
                    for (WorkerMetricEntity workerMetricEntity : offlines) {
                        if (log.isDebugEnabled()) {
                            log.debug("{} find offline id: {} lastHeartbeat:{}", TASK_NAME, workerMetricEntity.getWorkerId(), TimeFormateUtils.format(workerMetricEntity.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                        map.remove(workerMetricEntity.getWorkerId());
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }
    }

}
