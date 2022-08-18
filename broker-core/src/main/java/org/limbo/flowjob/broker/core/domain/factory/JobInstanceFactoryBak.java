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

package org.limbo.flowjob.broker.core.domain.factory;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.worker.Worker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-10-20
 */
public class JobInstanceFactoryBak {

    /**
     * 策略类型和策略生成器直接的映射
     */
    private final Map<JobType, JobInstanceCreator> creators;

    public JobInstanceFactoryBak(WorkerManager workerManager) {
        creators = new EnumMap<>(JobType.class);

        creators.put(JobType.NORMAL, new NormalJobInstanceCreator());
        creators.put(JobType.BROADCAST, new BroadcastJobInstanceCreator(workerManager));
        creators.put(JobType.MAP, new MapJobInstanceCreator());
        creators.put(JobType.MAP_REDUCE, new MapReduceJobInstanceCreator());
    }

    public JobInstance create(String planInstanceId, JobInfo jobInfo, LocalDateTime triggerAt) {
        return creators.get(jobInfo.getType()).create(planInstanceId, jobInfo, triggerAt);
    }

    /**
     * Task 创建策略接口，在这里对 Task 进行多种代理（装饰），实现下发重试策略。
     */
    public abstract class JobInstanceCreator {

        public JobInstance create(String planInstanceId, JobInfo jobInfo, LocalDateTime triggerAt) {
            if (!match(jobInfo)) {
                return null;
            }
            JobInstance instance = new JobInstance();
            instance.setPlanInstanceId(planInstanceId);
            instance.setJobId(jobInfo.getId());
            instance.setDispatchOption(jobInfo.getDispatchOption());
            instance.setExecutorOption(jobInfo.getExecutorOption());
            instance.setStatus(JobStatus.SCHEDULING);
            instance.setTriggerAt(triggerAt);
            instance.setAttributes(null); // todo 传递界面定义好的参数 传递上个节点传递的参数

            List<Task> tasks = tasks(instance);
            instance.setTasks(tasks);

            return instance;
        }

        public abstract List<Task> tasks(JobInstance instance);

        public boolean match(JobInfo jobInfo) {
            return jobInfo.getType() == getType();
        }

        public abstract JobType getType();

    }


    /**
     * 普通任务创建策略
     */
    public class NormalJobInstanceCreator extends JobInstanceCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            Task task = TaskFactory22.create(instance, TaskType.NORMAL);
            task.setAttributes(instance.getAttributes());
            return Collections.singletonList(task);
        }

        /**
         * 此策略仅适用于 {@link JobType#NORMAL} 类型的任务
         */
        @Override
        public JobType getType() {
            return JobType.NORMAL;
        }

    }


    /**
     * 广播任务创建策略
     */
    public class BroadcastJobInstanceCreator extends JobInstanceCreator {

        private final WorkerManager workerManager;

        public BroadcastJobInstanceCreator(WorkerManager workerManager) {
            this.workerManager = workerManager;
        }

        @Override
        public List<Task> tasks(JobInstance instance) {
            List<Task> tasks = new ArrayList<>();
            List<Worker> workers = workerManager.availableWorkers();
            if (CollectionUtils.isEmpty(workers)) {
                // todo 没有需要下发的任务 需要变更job状态 要过滤掉没有对应执行器的
            }
            for (Worker worker : workers) {
                Task task = TaskFactory22.create(instance, TaskType.BROADCAST);
                task.setAttributes(instance.getAttributes());
                task.setWorkerId(worker.getWorkerId());
                tasks.add(task);
            }
            return tasks;
        }

        /**
         * 此策略仅适用于 {@link JobType#BROADCAST} 类型的任务
         */
        @Override
        public JobType getType() {
            return JobType.BROADCAST;
        }

    }

    /**
     * 分片任务创建策略
     */
    public class MapJobInstanceCreator extends JobInstanceCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            Task mapTask = TaskFactory22.create(instance, TaskType.MAP);
            mapTask.setAttributes(instance.getAttributes());
            return Collections.singletonList(mapTask);
        }

        /**
         * 此策略仅适用于 {@link JobType#MAP} 类型的任务
         */
        @Override
        public JobType getType() {
            return JobType.MAP;
        }

    }

    /**
     * MapReduce任务创建策略
     */
    public class MapReduceJobInstanceCreator extends JobInstanceCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            Task mapTask = TaskFactory22.create(instance, TaskType.MAP);
            mapTask.setAttributes(instance.getAttributes());
            return Collections.singletonList(mapTask);
        }

        /**
         * 此策略仅适用于 {@link JobType#MAP} 类型的任务
         */
        @Override
        public JobType getType() {
            return JobType.MAP_REDUCE;
        }
    }

}
