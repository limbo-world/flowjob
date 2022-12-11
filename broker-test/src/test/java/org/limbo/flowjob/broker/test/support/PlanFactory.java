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

package org.limbo.flowjob.broker.test.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.limbo.flowjob.api.param.DispatchOptionParam;
import org.limbo.flowjob.api.param.JobAddParam;
import org.limbo.flowjob.api.param.PlanAddParam;
import org.limbo.flowjob.api.param.PlanReplaceParam;
import org.limbo.flowjob.api.param.ScheduleOptionParam;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.LoadBalanceType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.time.Duration;

/**
 * @author Devil
 * @since 2022/10/20
 */
public class PlanFactory {

    public static PlanAddParam newFixedRateAddParam() {
        PlanAddParam param = new PlanAddParam();
        param.setDescription("测试-固定速率");

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(5));
        scheduleOptionParam.setTriggerType(TriggerType.SCHEDULE);
        param.setScheduleOption(scheduleOptionParam);

        JobAddParam n1 = normalJob("1", "hello");
        n1.setChildrenIds(Sets.newHashSet("2"));
        JobAddParam n2 = normalJob("2", "hello");

        param.setJobs(Lists.newArrayList(n1, n2));
        return param;
    }

    public static PlanReplaceParam newFixedRateReplaceParam() {
        PlanReplaceParam param = new PlanReplaceParam();
        param.setDescription("测试-固定速率-replace");

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(3));
        scheduleOptionParam.setTriggerType(TriggerType.SCHEDULE);
        param.setScheduleOption(scheduleOptionParam);

        JobAddParam n1 = normalJob("1", "hello");
        n1.setChildrenIds(Sets.newHashSet("2"));
        JobAddParam n2 = normalJob("2", "hello");

        param.setJobs(Lists.newArrayList(n1, n2));
        return param;
    }

    public static JobAddParam normalJob(String id, String executorName) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setJobName("xxx");
        job.setDescription("test normal");
        job.setType(JobType.NORMAL);
        job.setTriggerType(TriggerType.SCHEDULE);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.RANDOM)
                .build()
        );
        job.setExecutorName(executorName);
        return job;
    }

    public static JobAddParam broadcastJob(String id, String executorName) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setDescription("test broadcast");
        job.setType(JobType.BROADCAST);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.ROUND_ROBIN)
                .build()
        );
        job.setExecutorName(executorName);
        return job;
    }

    public static JobAddParam mapJob(String id, String executorName) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setDescription("test map");
        job.setType(JobType.MAP);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.LEAST_RECENTLY_USED)
                .build()
        );
        job.setExecutorName(executorName);
        return job;
    }

    public static JobAddParam mapReduceJob(String id, String executorName) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setDescription("test reduce");
        job.setType(JobType.MAP_REDUCE);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.LEAST_RECENTLY_USED)
                .build()
        );
        job.setExecutorName(executorName);
        return job;
    }

}
