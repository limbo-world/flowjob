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
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreatorFactory;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class JobInstance implements Serializable {

    private static final long serialVersionUID = -4343833583716806197L;

    private String jobInstanceId;

    private String planId;

    private String planInstanceId;

    private String jobId;

    private DispatchOption dispatchOption;

    private ExecutorOption executorOption;

    public JobType type;

    /**
     * 状态
     */
    private JobScheduleStatus status;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    /**
     * 已经重试的次数 todo 可以不要这个字段，直接从db获取instance个数   不管用不用这个字段，可能存在worker重复反馈导致数据问题
     */
    private Integer retry;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 执行失败时候的处理
     */
    private JobFailHandler failHandler;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient TaskCreatorFactory taskCreatorFactory;


    /**
     * 是否能触发下级任务
     */
    public boolean canTriggerNext() {
        if (JobScheduleStatus.SUCCEED == status) {
            return true;
        } else if (JobScheduleStatus.FAILED == status) {
            // todo 根据 handler 类型来判断
            return true;
        } else {
            return false;
        }
    }

    /**
     * 作业下发，会先持久化任务，然后执行下发
     */
    public void dispatch() {
        List<Task> tasks = createTasks();
        for (Task task : tasks) {
            // 选择worker
            WorkerSelector workerSelector = WorkerSelectorFactory.newSelector(dispatchOption.getLoadBalanceType());

            // 执行下发
            boolean dispatched = task.dispatch(workerSelector);

            // 根据下发结果，更新作业实例状态
            if (dispatched) {
                dispatched();
            } else {
                dispatchFailed();
            }
        }
    }

    public List<Task> createTasks() {
        TaskCreatorFactory.TaskCreator taskCreator = taskCreatorFactory.get(type);
        return taskCreator.apply(this);
    }


    /**
     * 作业实例下发成功，更新状态为执行中
     */
    public void dispatched() {
        setStatus(JobScheduleStatus.EXECUTING);
    }


    /**
     * 作业实例下发失败
     */
    public void dispatchFailed() {
        setStatus(JobScheduleStatus.FAILED);
    }


    /**
     * 作业执行成功
     * @return 状态更新是否成功
     */
    public boolean succeed() {
        if (this.status != JobScheduleStatus.EXECUTING) {
            return false;
        }

        setStatus(JobScheduleStatus.SUCCEED);
        return true;
    }


    /**
     * 作业执行成功
     * @return 状态更新是否成功
     */
    public boolean failed() {
        if (this.status != JobScheduleStatus.EXECUTING) {
            return false;
        }

        setStatus(JobScheduleStatus.SUCCEED);
        return true;
    }


    public void handlerTaskSuccess(Task task) {
        task.succeed(); // todo 处理失败
        // 更新作业状态，更新失败说明处理过
        if (!succeed()) {
            return;
        }
    }


}
