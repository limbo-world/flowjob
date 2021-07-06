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

import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 基于RSocket协议通信的远程JobTracker
 * TODO
 *
 * @author Brozen
 * @since 2021-06-16
 */
public class RSocketRemoteJobTracker extends RemoteJobTracker {
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
    public Mono<Worker> registerWorker(Worker worker) {
        return null;
    }

    @Override
    public List<Worker> availableWorkers() {
        return null;
    }

    @Override
    public Mono<Worker> unregisterWorker(String workerId) {
        return null;
    }
}
