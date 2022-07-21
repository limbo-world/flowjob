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

package org.limbo.flowjob.broker.cluster;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.BrokerRegistry;
import org.limbo.flowjob.broker.core.cluster.NodeEvent;
import org.limbo.flowjob.broker.core.cluster.NodeListener;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.entity.BrokerEntity;
import org.limbo.flowjob.broker.dao.repositories.BrokerEntityRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 基于db的发布订阅
 *
 * @author Devil
 * @since 2022/7/15
 */
@Slf4j
public class DBBrokerRegistry implements BrokerRegistry {

    private final BrokerEntityRepo brokerEntityRepo;

    private BrokerConfig config;

    private long nodeStateCheckInterval = 400;

    private final List<NodeListener> listeners = new ArrayList<>();

    public DBBrokerRegistry(BrokerConfig config, BrokerEntityRepo brokerEntityRepo) {
        this.config = config;
        this.brokerEntityRepo = brokerEntityRepo;
    }

    @Override
    public void register(String host, int port) {
        // 开启定时任务 维持心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    BrokerEntity broker = new BrokerEntity();
                    broker.setHost(host);
                    broker.setPort(port);
                    broker.setLastHeartbeat(TimeUtil.nowLocalDateTime());
                    brokerEntityRepo.saveAndFlush(broker);
                } catch (Exception e) {
                    log.error("[HeartbeatTask] send heartbeat fail", e);
                }
            }
        }, 0, config.getHeartbeatInterval());

        // 开启定时任务，监听broker心跳情况
        new Timer().schedule(new HeartbeatCheckTask(), 0, nodeStateCheckInterval);
    }

    @Override
    public void subscribe(NodeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    private class HeartbeatCheckTask extends TimerTask {
        private final String taskName = "[HeartbeatCheckTask]";
        LocalDateTime lastOnlineCheckTime = TimeUtil.nowLocalDateTime().plusNanos(config.getHeartbeatTimeout());
        LocalDateTime lastOfflineCheckTime = TimeUtil.nowLocalDateTime().plusNanos(config.getHeartbeatTimeout());

        @Override
        public void run() {
            try {
                checkOnline();
                checkOffline();
            } catch (Exception e) {
                log.error("{} send heartbeat fail", taskName, e);
            }
        }

        private void checkOnline() {
            LocalDateTime startTime = lastOfflineCheckTime;
            LocalDateTime endTime = TimeUtil.nowLocalDateTime();
            log.info("{} checkOnline start:{} end:{}", taskName, startTime, endTime);
            List<BrokerEntity> onlineBrokers = brokerEntityRepo.findByLastHeartbeatBetween(startTime, endTime);
            if (CollectionUtils.isNotEmpty(onlineBrokers)) {
                for (BrokerEntity broker : onlineBrokers) {
                    for (NodeListener listener : listeners) {
                        listener.event(new NodeEvent(NodeEvent.Type.ONLINE, broker.getHost(), broker.getPort()));
                    }
                }
            }
            lastOnlineCheckTime = TimeUtil.nowLocalDateTime();
        }

        private void checkOffline() {
            LocalDateTime startTime = lastOfflineCheckTime;
            LocalDateTime endTime = TimeUtil.nowLocalDateTime().plusNanos(config.getHeartbeatTimeout());
            log.info("{} checkOnline start:{} end:{}", taskName, startTime, endTime);
            List<BrokerEntity> offlineBrokers = brokerEntityRepo.findByLastHeartbeatBetween(startTime, endTime);
            if (CollectionUtils.isNotEmpty(offlineBrokers)) {
                for (BrokerEntity broker : offlineBrokers) {
                    for (NodeListener listener : listeners) {
                        listener.event(new NodeEvent(NodeEvent.Type.OFFLINE, broker.getHost(), broker.getPort()));
                    }
                }
            }
            lastOnlineCheckTime = TimeUtil.nowLocalDateTime();
        }

    }

}
