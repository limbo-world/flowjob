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

package org.limbo.flowjob.agent.rpc;

import org.limbo.flowjob.agent.Job;
import org.limbo.flowjob.agent.ScheduleAgent;
import org.limbo.flowjob.common.meta.Worker;
import org.limbo.flowjob.common.exception.RegisterFailException;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Devil
 * @since 2023/8/4
 */
public interface AgentBrokerRpc {

    /**
     * 向 Broker 注册
     *
     * @param agent 需注册的 agent
     */
    void register(ScheduleAgent agent) throws RegisterFailException;

    /**
     * 维持心跳
     *
     * @param agent agent
     */
    void heartbeat(ScheduleAgent agent);

    /**
     * job已下发
     *
     * @return
     */
    boolean notifyJobDispatched(String jobInstanceId);

    /**
     * 向 Broker 反馈任务执行成功
     *
     * @param job 任务
     */
    void feedbackJobSucceed(Job job);

    /**
     * 向 Broker 反馈任务执行成功
     *
     * @param job 任务
     */
    void feedbackJobFail(Job job, @Nullable Throwable ex);

    /**
     * 获取 job 可以下发的所有worker
     *
     * @param jobId
     * @return
     */
    List<Worker> availableWorkers(String jobId);

}
