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

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.application.plan.service.TaskService;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.domain.ExecutorOption;
import org.limbo.flowjob.broker.core.domain.factory.TaskFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/8/18
 */
@Component
public class JobScheduler extends HashedWheelTimerScheduler<JobInstance> {

    @Setter(onMethod_ = @Inject)
    private TaskService taskService;
    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private WorkerManager workerManager;

    /**
     * 调度线程池
     */
    private final ExecutorService schedulePool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 8,
            Runtime.getRuntime().availableProcessors() * 8,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1024),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 由于是延迟触发的 调度前 自行保存 jobinstance信息
     *
     * @param jobInstance
     */
    @Override
    protected void doSchedule(JobInstance jobInstance) {
        // 执行调度逻辑
        schedulePool.submit(() -> {
            // 更新 job 为执行中
            int num = jobInstanceEntityRepo.updateStatus(
                    Long.valueOf(jobInstance.getJobInstanceId()),
                    JobStatus.SCHEDULING.status,
                    JobStatus.EXECUTING.status
            );

            if (num != 1) {
                return;
            }

            JobInstance.Tasks tasks = create(jobInstance);
            if (CollectionUtils.isEmpty(tasks.getTasks())) {
                // 如果没有任务 --- 一般是广播时候没有对应节点  job直接变为成功
                taskService.jobSuccess(jobInstance);
            } else {
                // 保存数据
                taskService.dispatch(jobInstance, tasks);
            }

            // 完成后移除
            unschedule(jobInstance.scheduleId());
        });
    }

    public JobInstance.Tasks create(JobInstance jobInstance) {
        List<Task> tasks = new ArrayList<>();
        switch (jobInstance.getType()) {
            case NORMAL:
                Task task = TaskFactory.create(jobInstance, TaskType.NORMAL);
                task.setAttributes(jobInstance.getAttributes());
                tasks.add(task);
                break;
            case BROADCAST:
                List<Worker> workers = workerManager.availableWorkers();
                if (CollectionUtils.isEmpty(workers)) {
                    break;
                }
                workers = workers.stream().filter(worker -> {
                    ExecutorOption executorOption = jobInstance.getExecutorOption();
                    for (WorkerExecutor executor : worker.getExecutors()) {
                        if (executorOption.getName().equals(executor.getName()) && executorOption.getType() == executor.getType()) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
                for (Worker worker : workers) {
                    Task broadcastTask = TaskFactory.create(jobInstance, TaskType.BROADCAST);
                    broadcastTask.setAttributes(jobInstance.getAttributes());
                    broadcastTask.setWorkerId(worker.getWorkerId());
                    tasks.add(broadcastTask);
                }
                break;
            case MAP:
            case MAP_REDUCE:
                Task mapTask = TaskFactory.create(jobInstance, TaskType.SPLIT);
                mapTask.setAttributes(jobInstance.getAttributes());
                tasks.add(mapTask);
                break;
        }
        return new JobInstance.Tasks(jobInstance.getJobInstanceId(), tasks);
    }

}
