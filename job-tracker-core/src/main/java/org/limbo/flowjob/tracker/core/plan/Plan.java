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

package org.limbo.flowjob.tracker.core.plan;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;

import java.time.Instant;
import java.util.List;

/**
 * 计划的抽象。一个计划{@link Plan}对应至少一个作业{@link Job}
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@Setter
@ToString
public class Plan implements Schedulable<Plan> {

    /**
     * 作业计划ID
     */
    private String planId;

    /**
     * 计划描述
     */
    private String planDesc;

    /**
     * 作业计划分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 此执行计划对应的所有作业
     */
    private List<Job> jobs;

    // -------- 需要注入
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private JobRepository jobRepository;

    /**
     * 作业触发计算器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private ScheduleCalculator triggerCalculator;


    public Plan(ScheduleCalculator triggerCalculator) {
        this.triggerCalculator = triggerCalculator;
    }


    /**
     * 计算作业下一次被触发时的时间戳。如果作业不会被触发，返回0或负数；
     * @return 作业下一次被触发时的时间戳，从1970-01-01 00:00:00到触发时刻的毫秒数。
     */
    public long nextTriggerAt() {
        return triggerCalculator.apply(this);
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getId() {
        return planId;
    }


    /**
     * TODO
     */
    @Override
    public Instant getLastScheduleAt() {
        return null;
    }


    /**
     * TODO
     * @return
     */
    @Override
    public Instant getLastFeedbackAt() {
        return null;
    }


    /**
     * 查询此plan下的job
     * @param jobId 作业ID
     * @return 作业领域
     */
    public Job getJob(String jobId) {
        return jobRepository.getJob(jobId);
    }
}
