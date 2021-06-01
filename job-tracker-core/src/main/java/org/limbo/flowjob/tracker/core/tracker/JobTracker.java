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


import static org.limbo.flowjob.tracker.core.tracker.SimpleJobTracker.*;

/**
 * 作业分发节点抽象。
 *
 * @author Brozen
 * @since 2021-05-16
 */
public interface JobTracker extends WorkerManager {

    /**
     * 启动JobTracker
     * @return 返回可用于关闭JobTracker的Disposable
     */
    DisposableJobTracker start();

    /**
     * 停止JobTracker
     */
    void stop();

    /**
     * 处于 {@link JobTrackerState#STARTED} 时会返回<code>true</code>。
     * @return JobTracker是否正在运行
     */
    boolean isRunning();

    /**
     * 处于 {@link JobTrackerState#STOPPING} 或 {@linkplain JobTrackerState#TERMINATED} 时会返回<code>true</code>。
     * @return JobTracker是否已停止
     */
    boolean isStopped();

    /**
     * 获取此JobTracker的生命周期监听注册器。
     * @return 生命周期监听注册器
     */
    JobTrackerLifecycle lifecycle();

}
