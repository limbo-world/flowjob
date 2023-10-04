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

package org.limbo.flowjob.test.core.dispatcher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.broker.core.schedule.selector.SingletonWorkerStatisticsRepo;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelectInvocation;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelector;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/9/22
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerSelectFactoryTest {

    private WorkerSelectorFactory factory;

    private WorkerSelectInvocation emptyInvocation;

    private List<Worker> workers;

    private WorkerStatisticsRepository workerStatisticsRepository;

    @BeforeAll
    public void init() throws MalformedURLException {
        workerStatisticsRepository = new SingletonWorkerStatisticsRepo();

        factory = new WorkerSelectorFactory();

        factory.setLbServerStatisticsProvider(workerStatisticsRepository);

        emptyInvocation = new WorkerSelectInvocation("test", new Attributes());

        workers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Worker worker = Worker.builder()
                    .id("w_" + i)
                    .url(new URL("http://127.0.0.1:8080"))
                    .executors(Collections.singletonList(new WorkerExecutor("test", "")))
                    .build();
            workers.add(worker);
        }

    }

    @Test
    void testRandom() {
        WorkerSelector workerSelector = factory.newSelector(LoadBalanceType.RANDOM);
        for (int i = 0; i < 30; i++) {
            Worker select = workerSelector.select(emptyInvocation, workers);
            System.out.println(select.getId());
        }
    }

    @Test
    void testRoundRobin() {
        WorkerSelector workerSelector = factory.newSelector(LoadBalanceType.ROUND_ROBIN);
        for (int i = 0; i < 30; i++) {
            Worker select = workerSelector.select(emptyInvocation, workers);
            System.out.println(select.getId());
        }
    }

    @Test
    void testAppoint() {
        Map<String, Object> map = new HashMap<>();
        map.put("worker.lb.appoint.byUrl", "12");
        map.put("worker.lb.appoint.byServerId", "w_2");
        WorkerSelectInvocation invocation = new WorkerSelectInvocation("test", new Attributes(map));
        WorkerSelector workerSelector = factory.newSelector(LoadBalanceType.APPOINT);
        for (int i = 0; i < 10; i++) {
            Worker select = workerSelector.select(invocation, workers);
            System.out.println(select.getId());
        }
    }

    @Test
    void testLeastFrequentlyUsed() {
        WorkerSelector workerSelector = factory.newSelector(LoadBalanceType.LEAST_FREQUENTLY_USED);
        for (int i = 0; i < 30; i++) {
            Worker select = workerSelector.select(emptyInvocation, workers);
            workerStatisticsRepository.recordDispatched(select);
            System.out.println(select.getId());
        }
    }

    @Test
    void testLeastRecentlyUsed() {
        WorkerSelector workerSelector = factory.newSelector(LoadBalanceType.LEAST_RECENTLY_USED);
        for (int i = 0; i < 30; i++) {
            Worker select = workerSelector.select(emptyInvocation, workers);
            workerStatisticsRepository.recordDispatched(select);
            System.out.println(select.getId());
        }
    }

    @Test
    void testConsistentHash() {
        WorkerSelector workerSelector = factory.newSelector(LoadBalanceType.CONSISTENT_HASH);
        for (int i = 0; i < 30; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("worker.lb.consistentHash.hashParamName", "hash");
            map.put("hash", i);
            WorkerSelectInvocation invocation = new WorkerSelectInvocation("test", new Attributes(map));
            Worker select = workerSelector.select(invocation, workers);
            workerStatisticsRepository.recordDispatched(select);
            System.out.println(select.getId());
        }
    }

}
