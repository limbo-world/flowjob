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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * todo
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisTaskRepo implements TaskRepository {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public void add(Task task) {

    }

    @Override
    public void executed(Task task) {
        taskMapper.update(null, Wrappers.<TaskPO>lambdaUpdate()
                .set(TaskPO::getState, task.getState().status)
                .set(TaskPO::getResult, task.getResult())
                .eq(TaskPO::getPlanId, task.getPlanId())
                .eq(TaskPO::getPlanRecordId, task.getPlanRecordId())
                .eq(TaskPO::getPlanInstanceId, task.getPlanInstanceId())
                .eq(TaskPO::getJobId, task.getJobId())
                .eq(TaskPO::getJobInstanceId, task.getJobInstanceId())
                .eq(TaskPO::getTaskId, task.getTaskId())
                .eq(TaskPO::getState, TaskScheduleStatus.EXECUTING.status)
                .eq(TaskPO::getResult, TaskResult.NONE.result)
        );
    }

    @Override
    public Integer unclosedCount(String planId, Long planRecordId, Long planInstanceId, String jobId, Long jobInstanceId) {
        return null;
    }

    @Override
    public Task get(String planId, Long planRecordId, Long planInstanceId, String jobId, Long jobInstanceId, String taskId) {
        return null;
    }

}
