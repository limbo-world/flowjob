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

package org.limbo.flowjob.broker.core.plan.job.context;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;

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


    /**
     * 状态
     */
    private JobScheduleStatus state;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    // ===== 非 po 属性

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


    // ---------------------- 需注入
    @ToString.Exclude
    @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Inject)
    private transient JobInstanceRepository jobInstanceRepo;


    /**
     * 获取此作业实例的下发配置
     */
    public DispatchOption getDispatchOption() {
        // TODO
        return null;
    }


    /**
     * 获取此作业实例的执行配置
     */
    public ExecutorOption getExecutorOption() {
        // TODO
        return null;
    }


    /**
     * TODO 获取此作业实例生成的任务类型
     */
    public TaskType getTaskType() {
        // TODO 哪里来？
        return TaskType.NORMAL;
    }


    /**
     * 是否能触发下级任务
     */
    public boolean canTriggerNext() {
        if (JobScheduleStatus.SUCCEED == state) {
            return true;
        } else if (JobScheduleStatus.FAILED == state) {
            // todo 根据 handler 类型来判断
            return true;
        } else {
            return false;
        }
    }


    /**
     * 作业实例下发成功，更新状态为执行中
     */
    public void dispatched() {
        setState(JobScheduleStatus.EXECUTING);
        jobInstanceRepo.dispatched(this);
    }


    /**
     * 作业实例下发失败
     */
    public void dispatchFailed() {
        setState(JobScheduleStatus.FAILED);
        jobInstanceRepo.dispatchFailed(this);
    }


    /**
     * 作业执行成功
     * @return 状态更新是否成功
     */
    public boolean succeed() {
        if (this.state != JobScheduleStatus.EXECUTING) {
            return false;
        }

        setState(JobScheduleStatus.SUCCEED);
        return jobInstanceRepo.executeSucceed(this);
    }


    /**
     * 作业执行成功
     * @return 状态更新是否成功
     */
    public boolean failed() {
        if (this.state != JobScheduleStatus.EXECUTING) {
            return false;
        }

        setState(JobScheduleStatus.SUCCEED);
        return jobInstanceRepo.executeSucceed(this);
    }

}
