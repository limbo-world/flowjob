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

import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.application.plan.service.JobService;
import org.limbo.flowjob.broker.application.plan.service.TaskService;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.repository.TasksRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;

import java.util.concurrent.ExecutorService;

/**
 * @author Devil
 * @since 2022/8/18
 */
public class JobScheduler extends HashedWheelTimerScheduler<JobInstance> {

    /**
     * 调度线程池
     */
    private final ExecutorService schedulePool;

    private TasksRepository tasksRepository;

    private JobService jobService;

    private TaskService taskService;

    private JobInstanceEntityRepo jobInstanceEntityRepo;

    private TaskEntityRepo taskEntityRepo;

    public JobScheduler(ExecutorService schedulePool) {
        super();
        this.schedulePool = schedulePool;
    }

    @Override
    public void schedule(JobInstance scheduled) {
        super.schedule(scheduled);
    }

    /**
     * 由于是延迟触发的 调度前 自行保存 jobinstance信息
     * @param jobInstance
     */
    @Override
    protected void doSchedule(JobInstance jobInstance) {
        // 执行调度逻辑
        schedulePool.submit(new Runnable() {
            @Override

            public void run() {
                JobInstance.Tasks tasks = new JobInstance.Tasks(jobInstance.getJobInstanceId(), null); // todo
                jobService.saveDispatchInfo(jobInstance, tasks);

                // 下发
                for (Task task : tasks.getTasks()) {
                    jobInstance.dispatch(task);

                    if (TaskStatus.EXECUTING == task.getStatus()) { // 成功
                        taskEntityRepo.updateStatus(Long.valueOf(task.getTaskId()),
                                TaskStatus.DISPATCHING.status,
                                TaskStatus.EXECUTING.status,
                                task.getWorkerId()
                        );
                    } else { // 失败
                        taskService.taskFail(task, "task dispatch fail", "");
                    }
                }

                // 完成后移除
                unschedule(jobInstance.scheduleId());
            }
        });
    }
}
