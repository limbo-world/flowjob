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
import org.limbo.flowjob.broker.core.cluster.NodeRegistry;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.URL;
import java.util.List;

/**
 * @author Devil
 * @since 2022/10/27
 */
public class BrokerStarter extends Broker implements ApplicationEventPublisherAware, ApplicationListener<ContextRefreshedEvent> {

    private final MetaTaskScheduler metaTaskScheduler;

    private final List<MetaTask> metaTasks;

    private ApplicationEventPublisher eventPublisher;

    public BrokerStarter(String name, URL baseUrl, NodeRegistry registry, NodeManger manger,
                         MetaTaskScheduler metaTaskScheduler, List<MetaTask> metaTasks) {
        super(name, baseUrl, registry, manger);
        this.metaTaskScheduler = metaTaskScheduler;
        this.metaTasks = metaTasks;
    }

    @Override
    public void start() {
        super.start();

        // 启动所有元任务调度
        metaTasks.forEach(metaTaskScheduler::schedule);

//        // 将自己注册为worker
//        eventPublisher.publishEvent(new WorkerReadyEvent());
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) { // 保证只执行一次
            start();
        }
    }
}
