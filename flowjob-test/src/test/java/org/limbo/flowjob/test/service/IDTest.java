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

package org.limbo.flowjob.test.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.limbo.flowjob.broker.core.context.IDGenerator;
import org.limbo.flowjob.broker.core.context.IDType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2023/9/22
 */
@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class IDTest {

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Test
    @Transactional
    void id() throws InterruptedException {
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                System.out.println(idGenerator.generateId(IDType.APP));
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                System.out.println(idGenerator.generateId(IDType.APP));
            }
        }).start();

        Thread.sleep(5000);
    }
}
