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

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;

import java.util.LinkedList;
import java.util.List;

/**
 * 主从模式下，主节点JobTracker
 * TODO
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Slf4j
public class LeaderJobTracker extends LocalJobTracker {

    /**
     * 当前节点是主节点时，此属性代表从节点tracker列表
     */
    protected List<RemoteJobTracker> followers;

    public LeaderJobTracker(String hostname, int port, WorkerRepository workerRepository) {
        super(hostname, port, workerRepository);
        this.followers = new LinkedList<>();
    }

}
