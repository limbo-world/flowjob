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

import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.processor.InstanceProcessorFactory;
import org.limbo.flowjob.broker.core.meta.task.JobExecuteCheckTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2024/1/14
 */
@Component
public class JobExecuteCheckTaskComponent extends JobExecuteCheckTask implements InitializingBean {


    public JobExecuteCheckTaskComponent(JobInstanceRepository jobInstanceRepository,
                                        @Lazy Broker broker,
                                        NodeManger nodeManger,
                                        InstanceProcessorFactory instanceProcessorFactory) {
        super(jobInstanceRepository, broker, nodeManger, instanceProcessorFactory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
