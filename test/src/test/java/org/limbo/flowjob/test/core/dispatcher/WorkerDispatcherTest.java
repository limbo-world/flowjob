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

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.dispatcher.FilteringWorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectArgument;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.constants.WorkerStatus;
import org.limbo.flowjob.common.lb.LBServerStatistics;
import org.limbo.flowjob.common.lb.LBServerStatisticsProvider;
import org.limbo.flowjob.common.lb.strategies.AppointLBStrategy;
import org.limbo.flowjob.common.lb.strategies.ConsistentHashLBStrategy;
import org.limbo.flowjob.common.lb.strategies.LFULBStrategy;
import org.limbo.flowjob.common.lb.strategies.LRULBStrategy;
import org.limbo.flowjob.common.lb.strategies.RandomLBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Brozen
 * @since 2023-01-29
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WorkerDispatcherTest {

    private List<Worker> workers;


    @BeforeAll
    public void init() throws MalformedURLException {
        workers = new ArrayList<>();
        workers.add(generateMockWorker("Worker1"));
        workers.add(generateMockWorker("Worker2"));
    }


    private Worker generateMockWorker(String id) throws MalformedURLException {
        return Worker.builder()
                .id(id)
                .name(id)
                .rpcBaseUrl(new URL("https://www.baidu.com"))
                .status(WorkerStatus.RUNNING)
                .isEnabled(true)
                .executors(Lists.newArrayList(
                        WorkerExecutor.builder()
                                .name("hello")
                                .build()
                ))
                .tags(new HashMap<>())
                .metric(new WorkerMetric(
                        Lists.newArrayList(),
                        new WorkerAvailableResource(4f, 8f, 10),
                        TimeUtils.currentLocalDateTime()
                ))
                .build();
    }


    @Test
    public void testRoundRobin() {
        FilteringWorkerSelector selector = new FilteringWorkerSelector(new RoundRobinLBStrategy<>());

        Worker w1 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w1.getId().equals("Worker1");
        Worker w2 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w2.getId().equals("Worker2");
        Worker w3 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w3.getId().equals("Worker1");
    }


    @Test
    public void testRandom() {
        FilteringWorkerSelector selector = new FilteringWorkerSelector(new RandomLBStrategy<>());
        assert selector.select(new MockWorkerSelectArgument(), workers) != null;
        assert selector.select(new MockWorkerSelectArgument(), workers) != null;
        assert selector.select(new MockWorkerSelectArgument(), workers) != null;
    }


    @Test
    public void testLFU() {
        List<LBServerStatistics> statistics = Lists.newArrayList(
                new MockLBServerStatistics("Worker1", Instant.EPOCH, 2),
                new MockLBServerStatistics("Worker2", Instant.EPOCH, 1)
        );
        MockWorkerStatisticsProvider provider = new MockWorkerStatisticsProvider(statistics);
        FilteringWorkerSelector selector = new FilteringWorkerSelector(new LFULBStrategy<>(provider));

        Worker w1 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w1.getId().equals("Worker2");
    }


    @Test
    public void testLRU() {
        List<LBServerStatistics> statistics = Lists.newArrayList(
                new MockLBServerStatistics("Worker1", Instant.EPOCH.plusSeconds(10), 2),
                new MockLBServerStatistics("Worker2", Instant.EPOCH, 1)
        );
        MockWorkerStatisticsProvider provider = new MockWorkerStatisticsProvider(statistics);
        FilteringWorkerSelector selector = new FilteringWorkerSelector(new LRULBStrategy<>(provider));

        Worker w1 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w1.getId().equals("Worker2");
    }


    @Test
    public void testAppoint() {
        FilteringWorkerSelector selector = new FilteringWorkerSelector(new AppointLBStrategy<>());
        MockWorkerSelectArgument args = new MockWorkerSelectArgument();

        args.getAttributes().put(AppointLBStrategy.PARAM_BY_SERVER_ID, "Worker1");
        Worker w1 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w1.getId().equals("Worker1");

        args.getAttributes().put(AppointLBStrategy.PARAM_BY_SERVER_ID, "Worker2");
        Worker w2 = selector.select(new MockWorkerSelectArgument(), workers);
        assert w2.getId().equals("Worker1");
    }


    @Test
    public void testConsistentHash() {
        FilteringWorkerSelector selector = new FilteringWorkerSelector(new ConsistentHashLBStrategy<>());
        MockWorkerSelectArgument args = new MockWorkerSelectArgument();
        args.getAttributes().put(ConsistentHashLBStrategy.HASH_PARAM_NAME, "hashKey");
        args.getAttributes().put("hashKey", "123");

        Worker w1 = selector.select(args, workers);
        assert w1 != null;

        Worker w2 = selector.select(args, workers);
        assert w1.equals(w2);

        Worker w3 = selector.select(args, workers);
        assert w1.equals(w3);
    }


    @Setter
    static class MockWorkerSelectArgument implements WorkerSelectArgument {

        private String executorName = "hello";

        private DispatchOption dispatchOption = DispatchOption.builder().build();

        private Map<String, String> attributes = new HashMap<>();

        @Override
        public String getExecutorName() {
            return executorName;
        }

        @Override
        public DispatchOption getDispatchOption() {
            return dispatchOption;
        }

        @Override
        public Map<String, String> getAttributes() {
            return attributes;
        }
    }


    @AllArgsConstructor
    static class MockWorkerStatisticsProvider implements LBServerStatisticsProvider {

        final List<LBServerStatistics> statistics;

        @Override
        public List<LBServerStatistics> getStatistics(Set<String> serverIds, Duration interval) {
            return statistics;
        }

    }

    @Getter
    @AllArgsConstructor
    static class MockLBServerStatistics implements LBServerStatistics {

        final String serverId;

        final Instant latestAccessAt;

        final int accessTimes;
    }

}
