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
import lombok.Setter;
import org.limbo.flowjob.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.api.console.param.WorkflowJobParam;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.LoadBalanceType;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author Devil
 * @since 2022/10/20
 */
@Component
public class PlanParamFactory {

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    public PlanParam newFixedRateAddParam() {
        PlanParam param = new PlanParam();
        param.setDescription("测试-固定速率");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(PlanType.WORKFLOW);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(5));
        param.setScheduleOption(scheduleOptionParam);

        WorkflowJobParam n1 = normalWorkflowJob(idGenerator.generateId(IDType.JOB_INFO), "hello");
        WorkflowJobParam n2 = normalWorkflowJob(idGenerator.generateId(IDType.JOB_INFO), "hello");

        n1.setChildren(Sets.newHashSet(n2.getId()));

        param.setWorkflow(Lists.newArrayList(n1, n2));
        return param;
    }

    public PlanParam newFixedRateReplaceParam() {
        PlanParam param = new PlanParam();
        param.setDescription("测试-固定速率-replace");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(PlanType.WORKFLOW);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(3));
        param.setScheduleOption(scheduleOptionParam);

        WorkflowJobParam n1 = normalWorkflowJob(idGenerator.generateId(IDType.JOB_INFO), "hello");
        WorkflowJobParam n2 = normalWorkflowJob(idGenerator.generateId(IDType.JOB_INFO), "hello");

        n1.setChildren(Sets.newHashSet(n2.getId()));

        param.setWorkflow(Lists.newArrayList(n1, n2));
        return param;
    }

    public WorkflowJobParam normalWorkflowJob(String id, String executorName) {
        WorkflowJobParam job = new WorkflowJobParam();
        job.setId(id);
        job.setName("test-normal-" + id);
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

    public static WorkflowJobParam broadcastWorkflowJob(String id, String executorName) {
        WorkflowJobParam job = new WorkflowJobParam();
        job.setId(id);
        job.setName("test-broadcast-" + id);
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

    public WorkflowJobParam mapWorkflowJob(String id, String executorName) {
        WorkflowJobParam job = new WorkflowJobParam();
        job.setId(id);
        job.setName("test-map-" + id);
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

    public WorkflowJobParam mapReduceWorkflowJob(String id, String executorName) {
        WorkflowJobParam job = new WorkflowJobParam();
        job.setId(id);
        job.setName("test-reduce-" + id);
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
