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

package org.limbo.flowjob.broker.core.agent.rpc;

import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.rpc.IRpc;

/**
 * Worker 通信接口
 *
 * @author Brozen
 * @since 2022-08-12
 */
public interface AgentRpc extends IRpc {

    /**
     * 发送一个作业到worker执行。当worker接受此task后，将触发返回
     * @param instance 作业实例
     * @return worker接受task后触发
     */
    boolean dispatch(JobInstance instance);

    /**
     * task查询
     * @param param 参数
     * @return 返回值
     */
    PageDTO<TaskDTO> page(TaskQueryParam param);

}
