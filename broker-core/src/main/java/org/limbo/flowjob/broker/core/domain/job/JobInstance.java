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

package org.limbo.flowjob.broker.core.domain.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.domain.DispatchOption;
import org.limbo.flowjob.broker.core.domain.ExecutorOption;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public class JobInstance implements Scheduled, Serializable {

    private static final long serialVersionUID = -7913375595969578408L;

    protected String jobInstanceId;

    protected String planVersion;

    protected String planInstanceId;

    protected String jobId;

    protected DispatchOption dispatchOption;

    protected ExecutorOption executorOption;

    protected JobType type;

    /**
     * 已经尝试的次数
     */
    protected Integer retry;

    /**
     * 状态
     */
    protected JobStatus status;

    /**
     * 触发时间
     */
    protected LocalDateTime triggerAt;

    /**
     * 开始时间
     */
    protected LocalDateTime startAt;

    /**
     * 结束时间
     */
    protected LocalDateTime endAt;

    /**
     * 此次执行的参数 界面配置
     */
    protected Attributes attributes;

    /**
     * 执行失败是否终止 false 会继续执行后续作业
     */
    private boolean terminateWithFail;

    /**
     * 是否已完成 可以触发下一个
     */
    public boolean isCompleted() {
        if (status == JobStatus.SUCCEED) {
            return true;
        }
        if (status == JobStatus.FAILED) {
            return !terminateWithFail;
        }
        return false;
    }

    public void dispatch(Task task) {
        // 选择worker
        WorkerSelector workerSelector = WorkerSelectorFactory.newSelector(dispatchOption.getLoadBalanceType());

        // 执行下发
        task.dispatch(workerSelector);
    }

    /**
     * 是否需要重试
     */
    public boolean retry() {
        if (dispatchOption.getRetry() > retry) {
            setTriggerAt(TimeUtil.currentLocalDateTime().plusSeconds(dispatchOption.getRetryInterval()));
            setJobInstanceId(null);
            setStatus(JobStatus.SCHEDULING);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String scheduleId() {
        return jobInstanceId;
    }

    @Override
    public LocalDateTime triggerAt() {
        return triggerAt;
    }

    @Getter
    @AllArgsConstructor
    public static class Tasks implements Serializable {

        private static final long serialVersionUID = 4739363562118991769L;

        private String jobInstanceId;

        private List<Task> tasks;
    }

}
