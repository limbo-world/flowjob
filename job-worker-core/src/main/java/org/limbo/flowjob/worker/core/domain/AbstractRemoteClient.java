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

package org.limbo.flowjob.worker.core.domain;

import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNode;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * worker与tracker建立连接 并实现 心跳 和 故障转移
 *
 * @author Devil
 * @date 2021/7/1 10:51 上午
 */
public abstract class AbstractRemoteClient {

    private final Worker worker;
    /**
     * 所有的tracker
     */
    private List<TrackerNode> trackers;
    /**
     * 是否已经启动
     */
    private volatile boolean started = false;

    public AbstractRemoteClient(Worker worker) {
        this.worker = worker;
    }

    public void start(String host, int port, int heartbeatPeriod) {
        // 已经启动则返回
        synchronized (this) {
            if (started) {
                return;
            }
        }
        // 建立连接
        clientStart(host, port);
        // 注册
        register(worker.register());
        // 心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                heartbeat(worker.heartbeat());
            }
        }, 200, heartbeatPeriod);
        // 启动完成
        started = true;
    }

    public abstract void clientStart(String host, int port);

    public abstract void heartbeat(WorkerHeartbeatOptionDto dto);

    public abstract void register(WorkerRegisterOptionDto dto);

}
