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

package org.limbo.flowjob.broker.application.plan.manager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2022/12/4
 */
@Slf4j
@Component
public class TaskScheduleManager {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Transactional
    public void dispatch(Task task) {
        // todo db 更新状态为下发中

        // 判断是否有workid 广播 会已经存在 其他应该在这里获取

        TaskDispatcher.dispatch(task);

        if (TaskStatus.FAILED == task.getStatus()) {
            // 下发失败
            taskEntityRepo.updateStatusFail(task.getTaskId(),
                    TaskStatus.EXECUTING.status,
                    TaskStatus.FAILED.status,
                    "dispatch fail",
                    ""
            );

            // 下发失败后要判断其他是否也失败 修改job状态
        } else {
            // 下发成功
            taskEntityRepo.updateStatusExecuting(task.getTaskId(), task.getWorkerId());
        }
    }

}
