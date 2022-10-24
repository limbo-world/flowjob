/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.starter;

import lombok.Setter;
import lombok.experimental.Delegate;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.starter.processor.event.ExecutorScannedEvent;
import org.limbo.flowjob.worker.starter.processor.event.WorkerReadyEvent;
import org.limbo.flowjob.worker.starter.properties.WorkerProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

/**
 * @author Brozen
 * @since 2022-09-11
 */
public class SpringDelegatedWorker implements Worker, DisposableBean {

    @Delegate(types = Worker.class)
    private final Worker delegated;

    @Setter(onMethod_ = @Autowired)
    private WorkerProperties properties;


    public SpringDelegatedWorker(Worker delegated) {
        this.delegated = delegated;
    }


    /**
     * 监听到 ExecutorScannedEvent 事件后，将 TaskExecutor 添加到 Worker
     */
    @EventListener(ExecutorScannedEvent.class)
    public void onExecutorScanned(ExecutorScannedEvent event) {
        event.getExecutors().forEach(delegated::addExecutor);
    }


    /**
     * 监听到 WorkerReadyEvent 事件后，注册并启动当前 Worker
     */
    @EventListener(WorkerReadyEvent.class)
    public void onWorkerReady(WorkerReadyEvent event) {
        delegated.start(properties.getHeartbeat());
    }


    /**
     * Bean 销毁时，停止 Worker
     */
    @Override
    public void destroy() {
        delegated.stop();
    }

}
