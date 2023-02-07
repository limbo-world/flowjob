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

package org.limbo.flowjob.broker.application.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.strategy.IScheduleStrategy;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.ExecuteResult;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2023/1/5
 */
@Slf4j
@Service
public class TaskService {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    @Setter(onMethod_ = @Inject)
    private IScheduleStrategy scheduleStrategy;

    /**
     * Worker任务执行反馈
     *
     * @param taskId 任务id
     * @param param  反馈参数
     */
    @Transactional
    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        ExecuteResult result = ExecuteResult.parse(param.getResult());
        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        TaskEntity taskEntity = taskEntityRepo.findById(taskId).orElse(null);
        Verifies.notNull(taskEntity, "task is null id:" + taskId);

        Task task = domainConverter.toTask(taskEntity);

        switch (result) {
            case SUCCEED:
                scheduleStrategy.handleTaskSuccess(task, param.getContext(), param.getResultData());
                break;

            case FAILED:
                scheduleStrategy.handleTaskFail(task, param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

}
