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

package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.dao.converter.TaskPoConverter;
import org.limbo.flowjob.broker.dao.mybatis.TaskMapper;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisTaskRepo implements TaskRepository {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskPoConverter converter;

    @Override
    public void add(Task task) {
        TaskEntity po = converter.convert(task);
        taskMapper.insert(po);
    }

    @Override
    public void executed(Task task) {
        taskMapper.update(null, Wrappers.<TaskEntity>lambdaUpdate()
                .set(TaskEntity::getState, TaskScheduleStatus.SCHEDULING.status)
                .eq(TaskEntity::getPlanId, task.getId().planId)
                .eq(TaskEntity::getPlanRecordId, task.getId().planRecordId)
                .eq(TaskEntity::getPlanInstanceId, task.getId().planInstanceId)
                .eq(TaskEntity::getJobId, task.getId().jobId)
                .eq(TaskEntity::getJobInstanceId, task.getId().jobInstanceId)
                .eq(TaskEntity::getTaskId, task.getId().taskId)
                .eq(TaskEntity::getState, TaskScheduleStatus.FEEDBACK.status)
                .eq(TaskEntity::getResult, TaskResult.NONE.result)
        );
    }

    @Override
    public void end(Task task) {
        taskMapper.update(null, Wrappers.<TaskEntity>lambdaUpdate()
                .set(TaskEntity::getState, TaskScheduleStatus.COMPLETED.status)
                .set(TaskEntity::getResult, task.getResult())
                .eq(TaskEntity::getPlanId, task.getId().planId)
                .eq(TaskEntity::getPlanRecordId, task.getId().planRecordId)
                .eq(TaskEntity::getPlanInstanceId, task.getId().planInstanceId)
                .eq(TaskEntity::getJobId, task.getId().jobId)
                .eq(TaskEntity::getJobInstanceId, task.getId().jobInstanceId)
                .eq(TaskEntity::getTaskId, task.getId().taskId)
                .eq(TaskEntity::getState, TaskScheduleStatus.FEEDBACK.status)
                .eq(TaskEntity::getResult, TaskResult.NONE.result)
        );
    }

    @Override
    public boolean execute(Task.ID taskId) {
        return taskMapper.update(null, Wrappers.<TaskEntity>lambdaUpdate()
                .set(TaskEntity::getState, TaskScheduleStatus.EXECUTING.status)
                .eq(TaskEntity::getPlanId, taskId.planId)
                .eq(TaskEntity::getPlanRecordId, taskId.planRecordId)
                .eq(TaskEntity::getPlanInstanceId, taskId.planInstanceId)
                .eq(TaskEntity::getJobId, taskId.jobId)
                .eq(TaskEntity::getJobInstanceId, taskId.jobInstanceId)
                .eq(TaskEntity::getTaskId, taskId.taskId)
                .eq(TaskEntity::getState, TaskScheduleStatus.SCHEDULING.status)
                .eq(TaskEntity::getResult, TaskResult.NONE.result)
        ) > 0;
    }

    @Override
    public Integer countUnclosed(Task.ID taskId) {
        return taskMapper.selectCount(Wrappers.<TaskEntity>lambdaQuery()
                .eq(TaskEntity::getPlanId, taskId.planId)
                .eq(TaskEntity::getPlanRecordId, taskId.planRecordId)
                .eq(TaskEntity::getPlanInstanceId, taskId.planInstanceId)
                .eq(TaskEntity::getJobId, taskId.jobId)
                .eq(TaskEntity::getJobInstanceId, taskId.jobInstanceId)
                .eq(TaskEntity::getState, TaskScheduleStatus.COMPLETED.status)
        );
    }

    @Override
    public Task get(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, String taskId) {
        TaskEntity po = taskMapper.selectOne(Wrappers.<TaskEntity>lambdaQuery()
                .eq(TaskEntity::getPlanId, planId)
                .eq(TaskEntity::getPlanRecordId, planRecordId)
                .eq(TaskEntity::getPlanInstanceId, planInstanceId)
                .eq(TaskEntity::getJobId, jobId)
                .eq(TaskEntity::getJobInstanceId, jobInstanceId)
                .eq(TaskEntity::getTaskId, taskId)
        );
        return converter.reverse().convert(po);
    }

}
