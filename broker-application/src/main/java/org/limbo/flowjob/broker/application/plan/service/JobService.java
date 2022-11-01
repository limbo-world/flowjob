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

package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.broker.application.plan.manager.PlanManager;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/9/1
 */
@Component
public class JobService {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private TaskFactory taskFactory;
    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;
    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanManager planManager;

    @Transactional
    public void dispatch(JobInstance instance) {
        // 更新 job 为执行中
        int num = jobInstanceEntityRepo.updateStatus(
                Long.valueOf(instance.getJobInstanceId()),
                JobStatus.SCHEDULING.status,
                JobStatus.EXECUTING.status
        );

        if (num != 1) {
            return;
        }

        List<Task> tasks = taskFactory.create(instance);
        if (CollectionUtils.isEmpty(tasks)) {
            handlerJobFail(instance);
        } else {
            dispatchTask(instance, tasks);
        }

    }

    @Transactional
    public void dispatchTask(JobInstance instance, List<Task> tasks) {

        taskRepository.saveAll(tasks);

        // 下发
        for (Task task : tasks) {

            TaskDispatcher.dispatch(task);

            if (TaskStatus.FAILED == task.getStatus()) {
                break;
            }
        }

        List<Task> dispatched = tasks.stream().filter(t -> TaskStatus.EXECUTING == t.getStatus()).collect(Collectors.toList());
        List<Task> dispatchFail = tasks.stream().filter(t -> TaskStatus.FAILED == t.getStatus() || TaskStatus.DISPATCHING == t.getStatus()).collect(Collectors.toList());

        // 下发成功的 根据执行完成回调处理
        for (Task task : dispatched) {
            taskEntityRepo.updateStatus(Long.valueOf(task.getTaskId()),
                    TaskStatus.DISPATCHING.status,
                    TaskStatus.EXECUTING.status,
                    task.getWorkerId()
            );
        }

        // 下发失败的
        List<Long> dispatchFailIds = dispatchFail.stream().map(t -> Long.valueOf(t.getTaskId())).collect(Collectors.toList());
        taskEntityRepo.updateStatusWithError(dispatchFailIds,
                TaskStatus.EXECUTING.status,
                TaskStatus.FAILED.status,
                "dispatch fail",
                ""
        );

        // 如果全部失败，根据策略来
        if (tasks.size() == dispatchFail.size()) {
            handlerJobFail(instance);
        }

    }

    @Transactional
    public void handlerJobFail(JobInstance instance) {
        if (instance.isTerminateWithFail()) {
            jobInstanceEntityRepo.updateStatus(
                    Long.valueOf(instance.getJobInstanceId()),
                    JobStatus.EXECUTING.status,
                    JobStatus.FAILED.status
            );

            planInstanceEntityRepo.end(
                    Long.valueOf(instance.getPlanInstanceId()),
                    PlanStatus.EXECUTING.status,
                    PlanStatus.FAILED.status,
                    TimeUtil.currentLocalDateTime()
            );
        } else {
            PlanInstance planInstance = planInstanceRepository.get(instance.getPlanInstanceId());
            planManager.dispatchNext(planInstance, instance.getJobId());
        }
    }


}
