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

package org.limbo.flowjob.broker.core.domain.task;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2022/8/17
 */
public class TaskFactory {

    /**
     * 策略类型和策略生成器直接的映射
     */
    private final Map<TaskType, TaskCreator> creators;

    private final WorkerManager workerManager;

    private final IDGenerator idGenerator;

    public TaskFactory(WorkerManager workerManager, IDGenerator idGenerator) {
        this.workerManager = workerManager;
        this.idGenerator = idGenerator;

        creators = new EnumMap<>(TaskType.class);

        creators.put(TaskType.NORMAL, new NormalTaskCreator());
        creators.put(TaskType.BROADCAST, new BroadcastTaskCreator());
        creators.put(TaskType.MAP, new MapTaskCreator());
        creators.put(TaskType.REDUCE, new ReduceTaskCreator());
        creators.put(TaskType.SPLIT, new SplitTaskCreator());
    }

    public List<Task> create(JobInstance instance, TaskType taskType) {
        TaskCreator creator = creators.get(taskType);
        if (creator == null) {
            return Collections.emptyList();
        }
        return creator.tasks(instance);
    }

    // todo
    /**
     * 获取上个任务
     */
    private List<TaskResult> preJobTaskResults(JobInstance instance) {
        // 获取上个节点
        instance.getJobId();

        // 获取所有task
        return null;
    }

    /**
     * Task 创建策略接口，在这里对 Task 进行多种代理（装饰），实现下发重试策略。
     */
    abstract static class TaskCreator {

        public abstract TaskType getType();

        public abstract List<Task> tasks(JobInstance instance);

        protected void initTask(Task task, JobInstance instance, List<Worker> availableWorkers) {
            task.setJobId(instance.getJobId());
            task.setPlanVersion(instance.getPlanVersion());
            task.setPlanId(instance.getPlanId());
            task.setJobInstanceId(instance.getJobInstanceId());
            task.setStatus(TaskStatus.DISPATCHING);
            task.setDispatchOption(instance.getDispatchOption());
            task.setExecutorName(instance.getExecutorName());
            task.setAttributes(instance.getAttributes());
            task.setAvailableWorkers(availableWorkers);
        }

    }

    /**
     * 普通任务创建策略
     */
    public class NormalTaskCreator extends TaskCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            Task task = new Task();
            task.setTaskId(idGenerator.generateId(IDType.TASK));
            initTask(task, instance, workerManager.availableWorkers());
            return Collections.singletonList(task);
        }

        /**
         * 此策略仅适用于 {@link TaskType#NORMAL} 类型的任务
         */
        @Override
        public TaskType getType() {
            return TaskType.NORMAL;
        }
    }

    /**
     * 广播任务创建策略
     */
    public class BroadcastTaskCreator extends TaskCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            List<Worker> workers = workerManager.availableWorkers();
            if (CollectionUtils.isEmpty(workers)) {
                return Collections.emptyList();
            }
            List<Task> tasks = new ArrayList<>();
            for (Worker worker : workers) {
                Task task = new Task();
                task.setTaskId(idGenerator.generateId(IDType.TASK));
                initTask(task, instance, Lists.newArrayList(worker));
                tasks.add(task);
            }
            return tasks;
        }

        /**
         * 此策略仅适用于 {@link TaskType#BROADCAST} 类型的任务
         */
        @Override
        public TaskType getType() {
            return TaskType.BROADCAST;
        }

    }

    /**
     * Split任务创建策略
     */
    public class SplitTaskCreator extends TaskCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            Task task = new Task();
            task.setTaskId(idGenerator.generateId(IDType.TASK));
            initTask(task, instance, workerManager.availableWorkers());
            return Collections.singletonList(task);
        }

        /**
         * 此策略仅适用于 {@link TaskType#SPLIT} 类型的任务
         */
        @Override
        public TaskType getType() {
            return TaskType.SPLIT;
        }

    }


    /**
     * Map任务创建策略
     */
    public class MapTaskCreator extends TaskCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            TaskResult taskResult = preJobTaskResults(instance).get(0);
            List<Worker> workers = workerManager.availableWorkers();
            List<Task> tasks = new ArrayList<>();
            for (Map<String, Object> attribute : taskResult.getSubTaskAttributes()) {
                MapTask task = new MapTask();
                task.setTaskId(idGenerator.generateId(IDType.TASK));
                initTask(task, instance, workers);
                task.setMapAttributes(new Attributes(attribute));
                tasks.add(task);
            }
            return tasks;
        }

        /**
         * 此策略仅适用于 {@link TaskType#MAP} 类型的任务
         */
        @Override
        public TaskType getType() {
            return TaskType.MAP;
        }

    }

    /**
     * Reduce任务创建策略
     */
    public class ReduceTaskCreator extends TaskCreator {

        @Override
        public List<Task> tasks(JobInstance instance) {
            List<TaskResult> taskResults = preJobTaskResults(instance);
            List<Attributes> reduceAttributes = new ArrayList<>();
            for (TaskResult taskResult : taskResults) {
                reduceAttributes.add(new Attributes(taskResult.getResultAttributes()));
            }

            ReduceTask task = new ReduceTask();
            task.setTaskId(idGenerator.generateId(IDType.TASK));
            initTask(task, instance, workerManager.availableWorkers());
            task.setReduceAttributes(reduceAttributes);
            return Collections.singletonList(task);
        }

        /**
         * 此策略仅适用于 {@link TaskType#REDUCE} 类型的任务
         */
        @Override
        public TaskType getType() {
            return TaskType.REDUCE;
        }
    }

}
