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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.TimerTask;

/**
 * task 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致task没有完成的原因
 * 1. worker服务真实下线
 * 2. worker服务假死
 * 3. worker完成task调用broker的接口失败
 */
@Component
public class TaskStatusCheckTask extends TimerTask {

    @Setter(onMethod_ = @Inject)
    private WorkerRepository workerRepository;

    @Setter(onMethod_ = @Inject)
    private WorkerSelector workerSelector;

    @Override
    public void run() {
        // todo 根据自己plan去查找
        List<Task> tasks = unCompleted();
        for (Task task : tasks) {
            if (!workerRepository.alive(task.getWorkerId())) {
                task.dispatch(workerSelector);
            }
        }

    }

    /**
     * 未完成的task
     */
    public List<Task> unCompleted() {
        // task在执行中 并且work是已下线状态的 --- 如果worker
        return null;
    }

}
