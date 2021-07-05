/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.infrastructure;

import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNode;

import java.util.List;

/**
 * @author Devil
 * @date 2021/7/5 11:05 上午
 */
public class RemoteClientCenter {

    private static AbstractRemoteClient client;
    /**
     * 所有的tracker
     */
    private List<TrackerNode> trackers;

    public void register(String host, int port, int heartbeatPeriod) {

    }

    public static AbstractRemoteClient getClient() {
        return client;
    }

}
