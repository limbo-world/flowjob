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

package org.limbo.flowjob.broker.core.plan.job;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventCenter;
import org.limbo.flowjob.broker.core.plan.ScheduleEventTopic;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreatorFactory;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;

import java.time.Instant;
import java.util.List;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public class JobInstance {

    private String jobInstanceId;

    private String planId;

    private String planInstanceId;

    private String jobId;

    private DispatchOption dispatchOption;

    private ExecutorOption executorOption;

    private JobType type;

    /**
     * 状态
     */
    private JobStatus status;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 执行失败时候的处理
     */
    private JobFailHandler failHandler;

    private List<Task> tasks;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private TaskCreatorFactory taskCreatorFactory;

    /**
     * 作业下发，会先持久化任务，然后执行下发
     */
    public void dispatch() {
        if (JobStatus.DISPATCHING != status) {
            return;
        }

        setStatus(JobStatus.EXECUTING);
        EventCenter.publish(new Event(this, ScheduleEventTopic.JOB_EXECUTING));

        // todo 这块逻辑有问题 广播和分片的处理逻辑没有包含
        this.tasks = createTasks();
        for (Task task : tasks) {
            // 选择worker
            WorkerSelector workerSelector = WorkerSelectorFactory.newSelector(dispatchOption.getLoadBalanceType());

            // 执行下发
            task.dispatch(workerSelector);
        }

    }

    public List<Task> createTasks() {
        TaskCreatorFactory.TaskCreator taskCreator = taskCreatorFactory.get(type);
        return taskCreator.apply(this);
    }

    /**
     * 作业执行成功
     * @return 状态更新是否成功
     */
    public void executeSucceed() {
        if (status != JobStatus.EXECUTING) {
            return;
        }
        setStatus(JobStatus.SUCCEED);

        EventCenter.publish(new Event(this, ScheduleEventTopic.JOB_SUCCESS));
    }

    /**
     * 执行失败
     */
    public void executeFail() {
        if (status != JobStatus.EXECUTING) {
            return;
        }
        setStatus(JobStatus.FAILED);

        EventCenter.publish(new Event(this, ScheduleEventTopic.JOB_FAIL));
    }


}
