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

package org.limbo.flowjob.tracker.core.tracker.bak;

import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import reactor.core.publisher.Mono;

/**
 * JobTracker声明周期函数
 *
 * @author Brozen
 * @since 2021-05-17
 */
public interface JobTrackerLifecycle {

    /**
     * 启动前。
     * @return start之前触发的Mono，可通过{@link DisposableJobTracker}阻止启动
     */
    Mono<DisposableJobTracker> beforeStart();

    /**
     * 启动成功后。
     * @return start之后触发的Mono，可通过{@link DisposableJobTracker}关闭JobTracker
     */
    Mono<DisposableJobTracker> afterStart();

    /**
     * 停止前。
     * @return 停止前触发的Mono，Mono触发时，将阻塞停止流程
     */
    Mono<JobTracker> beforeStop();

    /**
     * 停止后。
     * @return 停止后触发的Mono
     */
    Mono<JobTracker> afterStop();


    /**
     * JobTracker声明周期事件类型
     */
    enum JobTrackerLifecycleEventType {

        BEFORE_START,
        AFTER_START,
        BEFORE_STOP,
        AFTER_STOP

    }
}
