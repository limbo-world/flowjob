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

package org.limbo.flowjob.broker.core.meta.job;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Getter
@Builder
public class JobInstance implements Serializable {

    private static final long serialVersionUID = -7913375595969578408L;

    private String id;

    private String planInstanceId;

    private String planId;

    private String planVersion;

    private PlanType planType;

    /**
     * 当前是第几次重试
     */
    private int retryTimes = 0;

    /**
     * 绑定的broker
     */
    private URL brokerUrl;

    /**
     * agent ID
     */
    private String agentId;

    /**
     * 触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 全局上下文 todo 目前这个功能有点模糊，plan创建时候传入/流程中动态更新？？
     */
    private Attributes context;

    /**
     * job 参数 plan + job
     */
    private Attributes attributes;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    /**
     * 状态
     */
    private JobStatus status;

    /**
     * 任务信息
     */
    private WorkflowJobInfo jobInfo;

    /**
     * 设置为 retry 状态
     */
    public void retryReset(String id, Integer retryInterval) {
        if (!canRetry()) {
            throw new IllegalArgumentException("retry times limit");
        }
        this.triggerAt = TimeUtils.currentLocalDateTime().plusSeconds(retryInterval);
        this.id = id;
        this.status = JobStatus.SCHEDULING;
        this.retryTimes++;
    }

    /**
     * 是否能重试
     * @return 是否能重试
     */
    public boolean canRetry() {
        return retryTimes < jobInfo.getRetryOption().getRetry();
    }

}
