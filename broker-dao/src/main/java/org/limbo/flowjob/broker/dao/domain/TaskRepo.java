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

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.dao.converter.TaskPoConverter;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class TaskRepo implements TaskRepository {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Autowired
    private TaskPoConverter converter;

    @Setter(onMethod_ = @Inject)
    private IDRepo idRepo;


    /**
     * {@inheritDoc}
     *
     * @param task 作业执行实例
     * @return
     */
    @Override
    @Transactional
    public String add(Task task) {
        String taskId = idRepo.createTaskId();
        task.setTaskId(taskId);

        TaskEntity entity = converter.convert(task);
        taskEntityRepo.saveAndFlush(entity);
        return taskId;
    }


    /**
     * {@inheritDoc}
     *
     * @param task 任务
     * @return
     */
    @Override
    @Transactional
    public boolean dispatching(Task task) {
        return taskEntityRepo.updateState(task.getTaskId(),
                TaskScheduleStatus.SCHEDULING.status,
                TaskResult.NONE.result,
                TaskScheduleStatus.DISPATCHING.status,
                task.getWorkerId()
        ) > 0;
    }


    /**
     * {@inheritDoc}
     *
     * @param task 任务
     * @return
     */
    @Override
    @Transactional
    public boolean dispatched(Task task) {
        return taskEntityRepo.updateState(task.getTaskId(),
                TaskScheduleStatus.DISPATCHING.status,
                TaskResult.NONE.result,
                TaskScheduleStatus.EXECUTING.status,
                task.getWorkerId()
        ) > 0;
    }


    /**
     * {@inheritDoc}
     *
     * @param task 任务
     * @return
     */
    @Override
    @Transactional
    public boolean dispatchFailed(Task task) {
        return taskEntityRepo.updateState(task.getTaskId(),
                TaskScheduleStatus.DISPATCHING.status,
                TaskResult.NONE.result,
                TaskScheduleStatus.DISPATCH_FAILED.status,
                task.getWorkerId()
        ) > 0;
    }


    @Override
    @Transactional
    public void executed(String taskId) {
        taskEntityRepo.updateState(taskId,
                TaskScheduleStatus.SCHEDULING.status,
                TaskResult.NONE.result,
                TaskScheduleStatus.EXECUTING.status
        );
    }

    @Override
    @Transactional
    public void end(String taskId, TaskResult result) {
        taskEntityRepo.updateState(taskId,
                TaskScheduleStatus.SCHEDULING.status,
                TaskResult.NONE.result,
                TaskScheduleStatus.EXECUTING.status,
                result.result
        );
    }

    @Override
    public Long countByStates(String jobInstanceId, List<TaskScheduleStatus> statuses, List<TaskResult> results) {
        return taskEntityRepo.countByJobInstanceIdAndStateInAndResultIn(jobInstanceId,
                statuses.stream().map(s -> s.status).collect(Collectors.toList()),
                results.stream().map(r -> r.result).collect(Collectors.toList()));
    }

    @Override
    public Task get(String taskId) {
        return taskEntityRepo.findById(taskId).map(entity -> converter.reverse().convert(entity)).orElse(null);
    }

}
