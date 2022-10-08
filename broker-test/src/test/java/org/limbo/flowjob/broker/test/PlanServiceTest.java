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

package org.limbo.flowjob.broker.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.limbo.flowjob.broker.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.broker.api.console.param.ExecutorOptionParam;
import org.limbo.flowjob.broker.api.console.param.JobAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.api.constants.enums.LoadBalanceType;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Duration;

/**
 * @author Devil
 * @since 2022/9/2
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PlanServiceTest {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    @Test
    @Transactional
    public void addFixedRate() {
        PlanAddParam param = new PlanAddParam();
        param.setDescription("测试-固定速率");

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(5));
        scheduleOptionParam.setTriggerType(TriggerType.SCHEDULE);
        param.setScheduleOption(scheduleOptionParam);

        JobAddParam n1 = normalJob("1");
        n1.setChildrenIds(Sets.newHashSet("2"));
        JobAddParam n2 = normalJob("2");

        param.setJobs(Lists.newArrayList(n1, n2));

        planService.add(param);
    }

    @Test
    @Transactional
    public void replace() {
        PlanReplaceParam param = new PlanReplaceParam();

        planService.replace("", param);
    }


    public JobAddParam normalJob(String id) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setDescription("test normal");
        job.setType(JobType.NORMAL);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.RANDOM)
                .build()
        );
        job.setExecutorOption(executorOptionParam());
        return job;
    }

    public JobAddParam broadcastJob(String id) {
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
        job.setExecutorOption(executorOptionParam());
        return job;
    }

    public JobAddParam splitJob(String id) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setDescription("test split");
        job.setType(JobType.SPLIT);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.LEAST_FREQUENTLY_USED)
                .build()
        );
        job.setExecutorOption(executorOptionParam());
        return job;
    }

    public JobAddParam mapJob(String id) {
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
        job.setExecutorOption(executorOptionParam());
        return job;
    }

    public JobAddParam reduceJob(String id) {
        JobAddParam job = new JobAddParam();
        job.setJobId(id);
        job.setDescription("test reduce");
        job.setType(JobType.REDUCE);
        job.setDispatchOption(DispatchOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .loadBalanceType(LoadBalanceType.LEAST_RECENTLY_USED)
                .build()
        );
        job.setExecutorOption(executorOptionParam());
        return job;
    }

    public ExecutorOptionParam executorOptionParam() {
        return ExecutorOptionParam.builder()
                .type(JobExecuteType.FUNCTION)
                .name("hello")
                .build();
    }

}
