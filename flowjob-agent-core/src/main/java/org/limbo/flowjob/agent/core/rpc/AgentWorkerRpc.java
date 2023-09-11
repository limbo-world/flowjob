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

package org.limbo.flowjob.agent.core.rpc;

import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.Worker;

/**
 * @author Devil
 * @since 2023/8/4
 */
public interface AgentWorkerRpc {

    /**
     * 发送一个作业到worker执行。当worker接受此task后，将触发返回
     * @param worker 下发的worker
     * @param task 任务
     * @return worker接受task后触发
     */
    boolean dispatch(Worker worker, Task task);

}
