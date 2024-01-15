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

package org.limbo.flowjob.broker.core.meta.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.lock.DistributedLock;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2024/1/13
 */
@Slf4j
public class RebalanceTask {

    private final NodeManger nodeManger;

    private final DistributedLock lock;

    private final PlanRepository planRepository;

    private final JobInstanceRepository jobInstanceRepository;

    public RebalanceTask(NodeManger nodeManger,
                         DistributedLock lock,
                         PlanRepository planRepository,
                         JobInstanceRepository jobInstanceRepository) {
        this.nodeManger = nodeManger;
        this.lock = lock;
        this.planRepository = planRepository;
        this.jobInstanceRepository = jobInstanceRepository;
    }

    public void init() {
        new Timer().schedule(new InnerTask(), 0, Duration.ofSeconds(10).toMillis());
    }

    private class InnerTask extends TimerTask {

        private static final String PLAN_LOCK = "PLAN_LOCK";

        private static final String JOB_LOCK = "JOB_LOCK";

        @Override
        public void run() {

            try {
                if (CollectionUtils.isEmpty(nodeManger.allAlive())) {
                    return;
                }

                rebalancePlan();

                rebalanceJob();

            } catch (Exception e) {
                log.error("[RebalanceTask] run fail", e);
            }
        }

        private void rebalancePlan() {
            while (true) {
                if (!lock.tryLock(PLAN_LOCK, 5000)) {
                    return;
                }

                List<URL> brokerUrls = nodeManger.allAlive().stream().map(Node::getUrl).collect(Collectors.toList());
                Map<String, URL> notInBrokers = planRepository.findNotInBrokers(brokerUrls, 100);
                if (MapUtils.isEmpty(notInBrokers)) {
                    lock.unlock(PLAN_LOCK);
                    return;
                }

                for (Map.Entry<String, URL> entry : notInBrokers.entrySet()) {
                    String planId = entry.getKey();
                    URL url = entry.getValue();
                    // 如果重新上线了需要忽略
                    if (url != null && nodeManger.alive(url.toString())) {
                        continue;
                    }
                    Node elect = nodeManger.elect(planId);
                    planRepository.updateBroker(planId, url, elect.getUrl());
                }
                lock.unlock(PLAN_LOCK);
            }
        }

        private void rebalanceJob() {
            while (true) {
                if (!lock.tryLock(JOB_LOCK, 5000)) {
                    return;
                }

                List<URL> brokerUrls = nodeManger.allAlive().stream().map(Node::getUrl).collect(Collectors.toList());
                Map<String, URL> notInBrokers = jobInstanceRepository.findNotInBrokers(brokerUrls, 100);
                if (MapUtils.isEmpty(notInBrokers)) {
                    lock.unlock(JOB_LOCK);
                    return;
                }

                for (Map.Entry<String, URL> entry : notInBrokers.entrySet()) {
                    String jobInstanceId = entry.getKey();
                    URL url = entry.getValue();
                    // 如果重新上线了需要忽略
                    if (url != null && nodeManger.alive(url.toString())) {
                        continue;
                    }
                    Node elect = nodeManger.elect(jobInstanceId);
                    jobInstanceRepository.updateBroker(jobInstanceId, url, elect.getUrl());
                }
                lock.unlock(JOB_LOCK);
            }
        }

    }

}
