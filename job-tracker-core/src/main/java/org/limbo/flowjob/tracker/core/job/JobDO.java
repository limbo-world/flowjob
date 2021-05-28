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

import lombok.Setter;
import org.limbo.flowjob.tracker.commons.beans.domain.job.Job;
import org.limbo.flowjob.tracker.commons.beans.domain.job.JobContext;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.flowjob.tracker.core.job.schedule.JobScheduleCalculator;
import org.limbo.utils.UUIDUtils;

/**
 * 作业的抽象。主要定义了作业领域的的行为方法，属性的访问操作在{@link Job}轻量级领域对象中。
 *
 * @author Brozen
 * @since 2021-05-14
 */
public abstract class JobDO extends Job {


    /**
     * 作业触发计算器
     */
    @Setter
    private JobScheduleCalculator triggerCalculator;

    /**
     * 上下文repository
     */
    @Setter
    private JobContextRepository jobContextRepository;

    public JobDO(JobScheduleCalculator triggerCalculator,
                     JobContextRepository jobContextRepository) {

        this.triggerCalculator = triggerCalculator;
        this.jobContextRepository = jobContextRepository;
    }


    /**
     * 计算作业下一次被触发时的时间戳。如果作业不会被触发，返回0或负数；
     * @return 作业下一次被触发时的时间戳，从1970-01-01 00:00:00到触发时刻的毫秒数。
     */
    public long nextTriggerAt() {
        return triggerCalculator.apply(this);
    }

    /**
     * 生成新的作业执行上下文
     * @return 未开始执行的作业上下文
     */
    public JobContextDO newContext() {
        JobContextDO context = new JobContextDO(jobContextRepository);
        context.setJobId(getId());
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
        return jobContextRepository.getContext(getId(), contextId);
    }

}
