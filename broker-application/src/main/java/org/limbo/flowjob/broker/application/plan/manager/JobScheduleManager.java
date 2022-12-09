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
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.application.plan.component.TaskScheduler;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.TaskType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Devil
 * @since 2022/9/1
 */
@Component
public class JobScheduleManager {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private TaskFactory taskFactory;
    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;
    @Setter(onMethod_ = @Inject)
    private PlanManager planManager;
    @Setter(onMethod_ = @Inject)
    private TaskScheduler taskScheduler;

    @Transactional
    public void dispatch(JobInstance instance) {
        // 更新 job 为执行中
        int num = jobInstanceEntityRepo.updateStatusExecuting(instance.getJobInstanceId());

        if (num != 1) {
            return;
        }

        List<Task> tasks;
        switch (instance.getType()) {
            case NORMAL:
                tasks = taskFactory.create(instance, TaskType.NORMAL);
                break;
            case BROADCAST:
                tasks = taskFactory.create(instance, TaskType.BROADCAST);
                break;
            case MAP:
            case MAP_REDUCE:
                tasks = taskFactory.create(instance, TaskType.SPLIT);
                break;
            default:
                throw new JobException(instance.getJobId(), MsgConstants.UNKNOWN + " job type:" + instance.getType().type);
        }

        if (CollectionUtils.isEmpty(tasks)) {
            handlerJobFail(instance);
        } else {

            taskRepository.saveAll(tasks);

            for (Task task : tasks) {
                taskScheduler.schedule(task);
            }

//            dispatchTask(instance, tasks);
        }

    }

    @Transactional
    public void dispatchTask(JobInstance instance, List<Task> tasks) {

//        taskRepository.saveAll(tasks);

        for (Task task : tasks) {
            taskScheduler.schedule(task);
        }


//        // 下发
//        for (Task task : tasks) {
//
//            TaskDispatcher.dispatch(task);
//
//            if (TaskStatus.FAILED == task.getStatus()) {
//                break;
//            }
//        }
//
//        List<Task> dispatched = tasks.stream().filter(t -> TaskStatus.EXECUTING == t.getStatus()).collect(Collectors.toList());
//        List<Task> dispatchFail = tasks.stream().filter(t -> TaskStatus.FAILED == t.getStatus() || TaskStatus.DISPATCHING == t.getStatus()).collect(Collectors.toList());
//
//        // 下发成功的 根据执行完成回调处理
//        for (Task task : dispatched) {
//            taskEntityRepo.updateStatus(task.getTaskId(),
//                    TaskStatus.DISPATCHING.status,
//                    TaskStatus.EXECUTING.status,
//                    task.getWorkerId()
//            );
//        }
//
//        // 下发失败的
//        List<String> dispatchFailIds = dispatchFail.stream().map(Task::getTaskId).collect(Collectors.toList());
//        taskEntityRepo.updateStatusWithError(dispatchFailIds,
//                TaskStatus.EXECUTING.status,
//                TaskStatus.FAILED.status,
//                "dispatch fail",
//                ""
//        );
//
//        // 如果全部失败，根据策略来
//        if (tasks.size() == dispatchFail.size()) {
//            handlerJobFail(instance);
//        }

    }

    @Transactional
    public void handlerJobFail(JobInstance instance) {
        if (instance.isTerminateWithFail()) {
            jobInstanceEntityRepo.updateStatusExecuteFail(instance.getJobInstanceId(), MsgConstants.EMPTY_TASKS);
        } else {
            PlanInstance planInstance = planInstanceRepository.get(instance.getPlanInstanceId());
            planManager.dispatchNext(planInstance, instance.getJobId());
        }
    }


}
