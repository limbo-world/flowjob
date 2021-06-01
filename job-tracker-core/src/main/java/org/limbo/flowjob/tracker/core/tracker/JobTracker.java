/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.tracker;

import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import reactor.core.publisher.Mono;


/**
 * 作业分发节点抽象。
 *
 * @author Brozen
 * @since 2021-05-16
 */
public interface JobTracker extends WorkerManager, JobTrackerLifecycle {

    /**
     * 启动JobTracker
     * @return 返回可用于关闭JobTracker的Disposable
     */
    Mono<DisposableJobTracker> start();

    /**
     * 停止JobTracker
     * @return  返回关闭完成后触发的Mono
     */
    Mono<JobTracker> stop();

    /**
     * 获取此JobTracker的生命周期监听注册器。
     * @return 生命周期监听注册器
     */
    JobTrackerLifecycle lifecycle();

    /**
     * 获取作业分发器工厂
     * @return 作业分发器工厂
     */
    JobDispatcherFactory dispatcherFactory();

}
