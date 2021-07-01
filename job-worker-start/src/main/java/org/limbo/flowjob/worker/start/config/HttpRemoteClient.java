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

package org.limbo.flowjob.worker.start.config;

import okhttp3.OkHttpClient;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.worker.core.domain.AbstractRemoteClient;
import org.limbo.flowjob.worker.core.domain.Worker;

/**
 * @author Devil
 * @date 2021/7/1 11:01 上午
 */
public class HttpRemoteClient extends AbstractRemoteClient {

    private static OkHttpClient client;

    public HttpRemoteClient(Worker worker) {
        super(worker);
    }

    @Override
    public void clientStart(String port, int host) {
        client = (new OkHttpClient.Builder()).build();
    }

    @Override
    public void heartbeat(WorkerHeartbeatOptionDto dto) {

    }

    @Override
    public void register(WorkerRegisterOptionDto dto) {

    }
}
