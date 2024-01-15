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
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.meta.lock.DistributedLock;
import org.limbo.flowjob.broker.dao.entity.LockEntity;
import org.limbo.flowjob.broker.dao.repositories.LockEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.temporal.ChronoUnit;

/**
 * @author Devil
 * @since 2024/1/14
 */
@Slf4j
@Component
public class DatabaseDistributedLock implements DistributedLock {

    @Setter(onMethod_ = @Inject)
    private LockEntityRepo lockEntityRepo;

    @Setter(onMethod_ = @Inject)
    private Broker broker;

    @Override
    @Transactional
    public boolean tryLock(String name, long expire) {

        LockEntity lock = lockEntityRepo.findByName(name);

        String current = broker.getRpcBaseURL().toString();

        // 如果锁未过期且当前节点非加锁节点，加锁失败
        if (lock != null && lock.getExpireAt().isBefore(TimeUtils.currentLocalDateTime()) && !lock.getOwner().equals(current)) {
            return false;
        }

        if (lock == null) {
            lock = new LockEntity();
            lock.setName(name);
            lock.setOwner(current);
            lock.setExpireAt(TimeUtils.currentLocalDateTime().plus(expire, ChronoUnit.MILLIS));
            return dbLock(lock);
        } else {

            // 如果锁未过期且当前节点非加锁节点，加锁失败
            if (lock.getExpireAt().isBefore(TimeUtils.currentLocalDateTime()) && !lock.getOwner().equals(current)) {
                return false;
            }

            // 尝试加锁
            lock.setName(name);
            lock.setOwner(current);
            lock.setExpireAt(TimeUtils.currentLocalDateTime().plus(expire, ChronoUnit.MILLIS));
            return dbLock(lock);
        }
    }

    private boolean dbLock(LockEntity lock) {
        try {
            lockEntityRepo.saveAndFlush(lock);
            return true;
        } catch (DataIntegrityViolationException dive) {
            // 数据重复
            return false;
        } catch (Exception e) {
            log.warn("[DistributedLock] lock failed, name = {}.", lock.getName(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unlock(String name) {
        return lockEntityRepo.deleteByName(name) > 0;
    }
}
