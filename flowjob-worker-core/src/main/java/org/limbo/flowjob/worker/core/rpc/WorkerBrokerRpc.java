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

import org.limbo.flowjob.api.param.broker.DelayInstanceCommitParam;
import org.limbo.flowjob.api.param.broker.PlanInstanceCommitParam;
import org.limbo.flowjob.api.param.broker.PlanInstanceJobScheduleParam;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.worker.core.domain.Worker;

/**
 * @author Brozen
 * @since 2022-08-30
 */
public interface WorkerBrokerRpc {

    /**
     * 向 Broker 注册 Worker
     */
    String register() throws RegisterFailException;


    /**
     * 向 Broker 发送心跳
     */
    void heartbeat();

    void setWorker(Worker worker);

    /**
     * 基于Plan 创建实例
     * @param param 参数
     * @return id
     */
    String commitPlanInstance(PlanInstanceCommitParam param);

    /**
     * 触发PlanInstance下job执行
     * @param param 参数
     * @return id
     */
    String schedulePlanInstanceJob(PlanInstanceJobScheduleParam param);

    /**
     * 提交延迟任务
     * @param param 参数
     * @return id
     */
    String commitDelayInstance(DelayInstanceCommitParam.StandaloneParam param);

}
