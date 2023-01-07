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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public class JobInstance implements Serializable {

    private static final long serialVersionUID = -7913375595969578408L;

    protected String jobInstanceId;

    protected String planInstanceId;

    protected String planId;

    protected String planVersion;

    protected PlanType planType;

    protected String jobId;

    protected DispatchOption dispatchOption;

    protected String executorName;

    protected JobType type;

    protected TriggerType triggerType;

    /**
     * 已经尝试的次数
     */
    protected int retry = 0;

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
     * 是否需要重试 todo
     */
    public boolean retry() {
        if (dispatchOption.getRetry() > retry) {
            setTriggerAt(TimeUtils.currentLocalDateTime().plusSeconds(dispatchOption.getRetryInterval()));
            setJobInstanceId(null);
            setStatus(JobStatus.SCHEDULING);
            return true;
        } else {
            return false;
        }
    }

}
