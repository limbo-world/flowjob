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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.IdEntity;
import org.limbo.flowjob.broker.dao.repositories.IdEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Devil
 * @since 2022/11/26
 */
@Slf4j
@Component
public class IDGeneratorComponent implements IDGenerator {

    private static final Map<IDType, ID> ID_MAP = new ConcurrentHashMap<>();

    @Setter(onMethod_ = @Inject)
    private IdEntityRepo idEntityRepo;

    @Override
    @Transactional
    public String generateId(IDType type) {
        Long randomAutoId = gainRandomAutoId(type);
        return String.valueOf(randomAutoId);
    }

    private Long gainRandomAutoId(final IDType type) {
        synchronized (type) {
            ID id = ID_MAP.get(type);
            if (id == null || !id.valid()) {
                id = getNewId(type);
                ID_MAP.put(type, id);
            }
            return id.getCurrentId().incrementAndGet();
        }
    }

    private ID getNewId(IDType type) {
        Verifies.notNull(type, MsgConstants.UNKNOWN + " type: " + type);

        String typeName = type.name();

        int updateNum = 0; // 更新库存的条数
        int time = 0; // 重试次数

        long startId = 0;
        long endId = 0;

        while (updateNum <= 0 && time < 10) {
            // 加锁 获取 类型
            IdEntity idEntity = idEntityRepo.findById(typeName).orElse(null);
            if (idEntity == null) {
                throw new IllegalStateException(MsgConstants.UNKNOWN + " ID Type of " + typeName);
            }
            startId = idEntity.getCurrentId();
            endId = idEntity.getCurrentId() + idEntity.getStep();
            updateNum = idEntityRepo.casGainId(typeName, endId, startId);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("thread sleep fail", e);
                Thread.currentThread().interrupt();
            }
            time++;
        }

        if (updateNum <= 0) {
            throw new IllegalStateException("The system is busy, Try again later!!!");
        }

        ID id = new ID(new AtomicLong(startId), endId);
        ID_MAP.put(type, id);
        return id;
    }

    @Getter
    @AllArgsConstructor
    private static class ID {
        private AtomicLong currentId;

        private Long endId;

        public boolean valid() {
            return currentId.get() < endId;
        }
    }

}
