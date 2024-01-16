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

package org.limbo.flowjob.worker.demo.executors;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.param.broker.DelayInstanceCommitParam;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.limbo.flowjob.worker.core.rpc.WorkerBrokerRpc;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Devil
 * @since 2024/1/9
 */
@Slf4j
@Component
public class DelayInstanceCommitDemo {

    @Resource
    private WorkerBrokerRpc workerBrokerRpc;

    @PostConstruct
    public void init() {
        int delay = 5000;
//        int period = 5000;
        int period = 30000;
//        int period = 1800000;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    LocalDateTime triggerAt = TimeUtils.currentLocalDateTime().plusSeconds(20);
                    DelayInstanceCommitParam.StandaloneParam param = new DelayInstanceCommitParam.StandaloneParam();
                    param.setBizType("Test");
                    param.setBizId(System.currentTimeMillis() + "");
                    param.setTriggerAt(triggerAt);
                    param.setType(JobType.STANDALONE.type);
                    param.setExecutorName(HelloExecutorDemo.NAME);
                    String id = workerBrokerRpc.commitDelayInstance(param);
                    log.info("commit delay instance id:{}", id);
                } catch (Exception e) {
                    log.error("commit delay instance error", e);
                }
            }
        }, delay, period);
    }

}
