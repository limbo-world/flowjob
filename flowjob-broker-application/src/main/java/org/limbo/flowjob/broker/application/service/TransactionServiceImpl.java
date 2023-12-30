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

package org.limbo.flowjob.broker.application.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * @author Devil
 * @since 2023/12/29
 */
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Setter(onMethod_ = @Inject)
    private PlatformTransactionManager platformTransactionManager;

    @Override
    public <T> T transactional(Supplier<T> supplier) {
        TransactionStatus transaction = begin();
        try {
            T result = supplier.get();
            commit(transaction);
            return result;
        } catch (Exception e) {
            rollback(transaction, e);
            throw e;
        }
    }

    //开启事务, 默认使用RR隔离级别，REQUIRED传播级别
    private TransactionStatus begin() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        // 事物隔离级别，开启新事务
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        // 事务传播行为
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        //将拿到的事务返回进去，才能提交。
        return platformTransactionManager.getTransaction(def);
    }

    //提交事务
    private void commit(TransactionStatus transaction) {
        //提交事务
        platformTransactionManager.commit(transaction);
    }

    //回滚事务
    private void rollback(TransactionStatus transaction, Exception e) {
        platformTransactionManager.rollback(transaction);
        log.error("Transaction error and rollback", e);
    }

}
