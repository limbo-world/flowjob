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
import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.dao.converter.TaskPoConverter;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.mybatis.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

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

    @Setter(onMethod_ = @Inject)
    private IDRepo idRepo;

    @Override
    @Transactional
    public String add(Task task) {
        String taskId = idRepo.createTaskId();
        task.setTaskId(taskId);

        TaskEntity po = converter.convert(task);
        taskMapper.insert(po);
        return taskId;
    }

    @Override
    @Transactional
    public void executed(String taskId) {
        taskMapper.update(null, Wrappers.<TaskEntity>lambdaUpdate()
                .set(TaskEntity::getState, TaskScheduleStatus.FEEDBACK.status)
                .eq(TaskEntity::getTaskId, taskId)
                .eq(TaskEntity::getState, TaskScheduleStatus.SCHEDULING.status)
                .eq(TaskEntity::getResult, TaskResult.NONE.result)
        );
    }

    @Override
    @Transactional
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
    public boolean execute(String taskId) {
        return taskMapper.update(null, Wrappers.<TaskEntity>lambdaUpdate()
                .set(TaskEntity::getState, TaskScheduleStatus.EXECUTING.status)
                .eq(TaskEntity::getTaskId, taskId)
                .eq(TaskEntity::getState, TaskScheduleStatus.SCHEDULING.status)
                .eq(TaskEntity::getResult, TaskResult.NONE.result)
        ) > 0;
    }

    @Override
    public Integer countByStates(String jobInstanceId, List<Byte> states, List<Byte> results) {
        return taskMapper.selectCount(Wrappers.<TaskEntity>lambdaQuery()
                .eq(TaskEntity::getJobInstanceId, jobInstanceId)
                .in(TaskEntity::getState, states)
                .in(TaskEntity::getResult, results)
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
