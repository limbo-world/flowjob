/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.core.rpc;

import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.dto.BrokerDTO;

import java.util.List;

/**
 * @author Brozen
 * @since 2022-08-30
 */
public interface BrokerRpc {

    /**
     * 更新 Broker 节点信息
     * @param brokers Broker 节点列表
     */
    void updateBrokerNodes(List<BrokerDTO> brokers);

    /**
     * 向 Broker 注册 Worker
     * @param param 注册参数
     * @return 注册结果
     */
    WorkerRegisterDTO register(WorkerRegisterParam param);

    /**
     * 向 Broker 发送心跳
     * @param param 心跳参数
     */
    void heartbeat(WorkerHeartbeatParam param);

    /**
     * 向 Broker 反馈任务执行结果
     * @param param 执行结果
     */
    void feedbackTask(TaskFeedbackParam param);
}
