/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core;

import org.limbo.flowjob.tracker.core.constants.JobDispatchType;
import org.limbo.flowjob.tracker.core.dispatcher.JobDispatcher;

import java.util.Collection;

/**
 * 作业执行节点选择器。对应{@link JobDispatcher}
 *
 * @author Brozen
 * @since 2021-05-16
 */
public interface WorkerChooser {

    /**
     * 选择一个worker作为执行
     * @param workers 全部待选择的执行节点
     * @param dispatchType 作业的分发方式
     * @return 选择的执行作业的worker
     */
    Worker chooseWorker(Collection<Worker> workers, JobDispatchType dispatchType);

}
