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
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public abstract class JobInstance implements Serializable {

    private static final long serialVersionUID = -7913375595969578408L;

    protected String jobInstanceId;

    protected String planInstanceId;

    protected String planId;

    protected String planVersion;

    protected PlanType planType;

    /**
     * 触发时间
     */
    protected LocalDateTime triggerAt;

    /**
     * 全局上下文
     */
    private Attributes context;

    /**
     * job 节点动态参数
     */
    private Attributes jobAttributes;

    /**
     * 开始时间
     */
    protected LocalDateTime startAt;

    /**
     * 结束时间
     */
    protected LocalDateTime endAt;

    /**
     * 状态
     */
    protected JobStatus status;

    public abstract JobInfo getJobInfo();
}
