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

package org.limbo.flowjob.broker.core.worker.rpc;

import org.limbo.flowjob.common.constants.Protocol;
import org.limbo.flowjob.common.constants.WorkerStatus;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;

/**
 * Worker 通信接口
 *
 * @author Brozen
 * @since 2022-08-12
 */
public interface WorkerRpc {

    /**
     * 发送一个作业到worker执行。当worker接受此task后，将触发返回
     * @param task 作业实例
     * @return worker接受task后触发
     */
    boolean sendTask(Task task);

    /**
     * 解注册此worker，worker的状态将被标记为{@link WorkerStatus#TERMINATED}
     */
    void unregister();

    String workerId();

    Protocol protocol();

    String host();

    Integer port();

}
