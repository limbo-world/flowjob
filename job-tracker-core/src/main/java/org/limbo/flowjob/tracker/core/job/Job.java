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

package org.limbo.flowjob.tracker.core.job;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.utils.UUIDUtils;

import java.util.List;

/**
 * 作业的抽象。主要定义了作业领域的的行为方法，属性的访问操作在{@link Job}轻量级领域对象中。
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class Job {

    /**
     * 作业所属计划ID
     */
    private String planId;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 作业描述
     */
    private String jobDesc;

    /**
     * 此作业依赖的父作业ID
     */
    private List<String> parentJobIds;

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业调度配置参数
     */
    private ScheduleOption scheduleOption;

    // ----------------- 需要注入

    /**
     * 上下文repository
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private JobContextRepository jobContextRepository;

    public Job(JobContextRepository jobContextRepository) {
        this.jobContextRepository = jobContextRepository;
    }

    /**
     * 生成新的作业执行上下文
     * @return 未开始执行的作业上下文
     */
    public JobContext newContext() {
        JobContext context = new JobContext(jobContextRepository);
        context.setJobId(getJobId());
        context.setContextId(UUIDUtils.randomID());
        context.setStatus(JobContextStatus.INIT);
        context.setJobAttributes(null);
        jobContextRepository.addContext(context);
        return context;
    }

    /**
     * 获取作业的执行上下文
     * @param contextId 上下文ID
     * @return 作业执行上下文
     */
    public JobContext getContext(String contextId) {
        return jobContextRepository.getContext(getJobId(), contextId);
    }

}
