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

package org.limbo.flowjob.tracker.infrastructure.job.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.commons.constants.enums.TaskResult;
import org.limbo.flowjob.tracker.commons.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.job.context.TaskRepository;
import org.limbo.flowjob.tracker.dao.mybatis.TaskMapper;
import org.limbo.flowjob.tracker.dao.po.TaskPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.TaskPoConverter;
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
        TaskPO po = converter.convert(task);
        taskMapper.insert(po);
    }

    @Override
    public void executed(Task task) {
        taskMapper.update(null, Wrappers.<TaskPO>lambdaUpdate()
                .set(TaskPO::getState, TaskScheduleStatus.SCHEDULING.status)
                .eq(TaskPO::getPlanId, task.getId().planId)
                .eq(TaskPO::getPlanRecordId, task.getId().planRecordId)
                .eq(TaskPO::getPlanInstanceId, task.getId().planInstanceId)
                .eq(TaskPO::getJobId, task.getId().jobId)
                .eq(TaskPO::getJobInstanceId, task.getId().jobInstanceId)
                .eq(TaskPO::getTaskId, task.getId().taskId)
                .eq(TaskPO::getState, TaskScheduleStatus.FEEDBACK.status)
                .eq(TaskPO::getResult, TaskResult.NONE.result)
        );
    }

    @Override
    public void end(Task task) {
        taskMapper.update(null, Wrappers.<TaskPO>lambdaUpdate()
                .set(TaskPO::getState, TaskScheduleStatus.COMPLETED.status)
                .set(TaskPO::getResult, task.getResult())
                .eq(TaskPO::getPlanId, task.getId().planId)
                .eq(TaskPO::getPlanRecordId, task.getId().planRecordId)
                .eq(TaskPO::getPlanInstanceId, task.getId().planInstanceId)
                .eq(TaskPO::getJobId, task.getId().jobId)
                .eq(TaskPO::getJobInstanceId, task.getId().jobInstanceId)
                .eq(TaskPO::getTaskId, task.getId().taskId)
                .eq(TaskPO::getState, TaskScheduleStatus.FEEDBACK.status)
                .eq(TaskPO::getResult, TaskResult.NONE.result)
        );
    }

    @Override
    public boolean execute(Task.ID taskId) {
        return taskMapper.update(null, Wrappers.<TaskPO>lambdaUpdate()
                .set(TaskPO::getState, TaskScheduleStatus.EXECUTING.status)
                .eq(TaskPO::getPlanId, taskId.planId)
                .eq(TaskPO::getPlanRecordId, taskId.planRecordId)
                .eq(TaskPO::getPlanInstanceId, taskId.planInstanceId)
                .eq(TaskPO::getJobId, taskId.jobId)
                .eq(TaskPO::getJobInstanceId, taskId.jobInstanceId)
                .eq(TaskPO::getTaskId, taskId.taskId)
                .eq(TaskPO::getState, TaskScheduleStatus.SCHEDULING.status)
                .eq(TaskPO::getResult, TaskResult.NONE.result)
        ) > 0;
    }

    @Override
    public Integer countUnclosed(Task.ID taskId) {
        return taskMapper.selectCount(Wrappers.<TaskPO>lambdaQuery()
                .eq(TaskPO::getPlanId, taskId.planId)
                .eq(TaskPO::getPlanRecordId, taskId.planRecordId)
                .eq(TaskPO::getPlanInstanceId, taskId.planInstanceId)
                .eq(TaskPO::getJobId, taskId.jobId)
                .eq(TaskPO::getJobInstanceId, taskId.jobInstanceId)
                .eq(TaskPO::getState, TaskScheduleStatus.COMPLETED.status)
        );
    }

    @Override
    public Task get(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, String taskId) {
        TaskPO po = taskMapper.selectOne(Wrappers.<TaskPO>lambdaQuery()
                .eq(TaskPO::getPlanId, planId)
                .eq(TaskPO::getPlanRecordId, planRecordId)
                .eq(TaskPO::getPlanInstanceId, planInstanceId)
                .eq(TaskPO::getJobId, jobId)
                .eq(TaskPO::getJobInstanceId, jobInstanceId)
                .eq(TaskPO::getTaskId, taskId)
        );
        return converter.reverse().convert(po);
    }

}
