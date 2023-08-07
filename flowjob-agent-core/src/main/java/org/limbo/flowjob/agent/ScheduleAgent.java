/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.agent;

import java.net.URL;
import java.time.Duration;

/**
 * @author Devil
 * @since 2023/8/4
 */
public interface ScheduleAgent {

    /**
     * 获取通信用 URL
     */
    URL getURL();

    /**
     * 启动
     *
     * @param heartbeatPeriod 心跳间隔
     */
    void start(Duration heartbeatPeriod);

    /**
     * Just beat it
     * 发送心跳
     */
    void sendHeartbeat();

    /**
     * 接收 Broker 发送来的任务
     * @param job 任务数据
     */
    void receiveJob(Job job);

    /**
     * 停止当前 Worker
     */
    void stop();

    /**
     * 队列可用
     */
    int availableQueueSize();

}
