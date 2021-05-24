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

package org.limbo.flowjob.tracker.core.job.schedule;

import org.limbo.flowjob.tracker.core.commons.StrategyFactory;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobContextRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-20
 */
public class JobScheduleCalculatorFactory implements StrategyFactory<Job, JobScheduleCalculator, Job, Long> {

    /**
     * 全部策略
     */
    private List<JobScheduleCalculator> jobScheduleCalculators;

    public JobScheduleCalculatorFactory(JobContextRepository jobContextRepository) {
        ArrayList<JobScheduleCalculator> calculators = new ArrayList<>();

        // 预设计算器
        calculators.add(new DelayedJobScheduleCalculator(jobContextRepository));
        calculators.add(new FixRateJobScheduleCalculator(jobContextRepository));
        calculators.add(new FixIntervalJobScheduleCalculator(jobContextRepository));

        // 自定义计算器

        // 配置不可变
        jobScheduleCalculators = Collections.unmodifiableList(calculators);

    }


    /**
     * 根据作业调度类型，创建作业触发时间计算器
     * @param job 根据此作业创建触发时间计算器
     * @return 触发时间计算器
     */
    @Override
    public JobScheduleCalculator newStrategy(Job job) {
        for (JobScheduleCalculator calculator : jobScheduleCalculators) {
            if (calculator.canApply(job)) {
                return calculator;
            }
        }

        throw new IllegalStateException("cannot apply JobTriggerCalculator for job " + job);
    }

}
