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

import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;

/**
 * 主从模式下，从节点JobTracker。从节点接受到的worker请求，需要通过从节点，将请求转发到主节点上。
 * TODO
 * @author Brozen
 * @since 2021-06-16
 */
public class FollowerJobTracker extends LocalJobTracker {

    /**
     * 当前节点是从节点时，此属性代表主节点tracker
     */
    protected RemoteJobTracker leader;

    public FollowerJobTracker(String hostname, int port, WorkerRepository workerRepository) {
        super(hostname, port, workerRepository);
    }

    @Override
    public DisposableJobTracker start() {
        return null;
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
}
