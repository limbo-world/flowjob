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

package org.limbo.flowjob.broker.dao.domain;

import org.limbo.flowjob.broker.api.dto.BrokerDTO;
import org.limbo.flowjob.broker.cluster.BrokerRepository;
import org.limbo.flowjob.broker.cluster.Constants;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.entity.BrokerEntity;
import org.limbo.flowjob.broker.dao.repositories.BrokerEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Devil
 * @since 2022/7/18
 */
@Repository
public class BrokerRepo implements BrokerRepository {

    @Autowired
    private BrokerEntityRepo brokerEntityRepo;

    @Override
    public void heartbeat(String host, int port) {
        BrokerEntity broker = new BrokerEntity();
        broker.setHost(host);
        broker.setPort(port);
        broker.setLastHeartbeat(TimeUtil.nowLocalDateTime());
        brokerEntityRepo.saveAndFlush(broker);
    }

    @Override
    public List<BrokerDTO> online(LocalDateTime lastHeartbeat) {
        List<BrokerEntity> brokerEntities = brokerEntityRepo.findByLastHeartbeatGreaterThan(lastHeartbeat);
        return null; // todo
    }
}
