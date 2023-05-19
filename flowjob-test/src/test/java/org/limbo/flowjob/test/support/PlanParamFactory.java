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

package org.limbo.flowjob.test.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.limbo.flowjob.api.param.console.DispatchOptionParam;
import org.limbo.flowjob.api.param.console.JobParam;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.RetryOptionParam;
import org.limbo.flowjob.api.param.console.ScheduleOptionParam;
import org.limbo.flowjob.api.param.console.WorkflowJobParam;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.common.utils.UUIDUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.limbo.flowjob.test.util.DAGTest;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Devil
 * @since 2022/10/20
 */
public class PlanParamFactory {

    public static PlanParam newMapReduceAddParam(PlanType planType) {
        PlanParam param = new PlanParam();
        param.setName(UUIDUtils.randomID());
        param.setDescription("测试-固定速率");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(planType);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(5));
        param.setScheduleOption(scheduleOptionParam);

        if (PlanType.NORMAL == planType) {
            param.setJob(newJob("MapReduceExecutorDemo", JobType.MAP_REDUCE));
        } else {
            WorkflowJobParam n1 = newWorkflowJob(UUIDUtils.shortRandomID(), "MapReduceExecutorDemo", JobType.MAP_REDUCE, TriggerType.SCHEDULE);
            WorkflowJobParam n2 = newWorkflowJob(UUIDUtils.shortRandomID(), "MapReduceExecutorDemo", JobType.MAP_REDUCE, TriggerType.SCHEDULE);

            n1.setChildren(Sets.newHashSet(n2.getId()));

            param.setWorkflow(Lists.newArrayList(n1, n2));
        }
        return param;
    }

    public static PlanParam newWorkflowParam(TriggerType triggerType) {
        PlanParam param = new PlanParam();
        param.setName(UUIDUtils.randomID());
        param.setDescription("测试-固定速率");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(PlanType.WORKFLOW);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(5));
        param.setScheduleOption(scheduleOptionParam);

        WorkflowJobParam n1 = newWorkflowJob(UUIDUtils.shortRandomID(), "MapReduceExecutorDemo", JobType.MAP_REDUCE, TriggerType.SCHEDULE);
        WorkflowJobParam n2 = newWorkflowJob(UUIDUtils.shortRandomID(), "MapReduceExecutorDemo", JobType.MAP_REDUCE, triggerType);

        n1.setChildren(Sets.newHashSet(n2.getId()));

        param.setWorkflow(Lists.newArrayList(n1, n2));
        return param;
    }

    public static PlanParam newFixedRateAddParam(PlanType planType) {
        PlanParam param = new PlanParam();
        param.setName(UUIDUtils.randomID());
        param.setDescription("测试-固定速率");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(planType);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(3));
        param.setScheduleOption(scheduleOptionParam);

        if (PlanType.NORMAL == planType) {
            param.setJob(newJob("hello", JobType.NORMAL));
        } else {
            WorkflowJobParam n1 = newWorkflowJob(UUIDUtils.shortRandomID(), "hello", JobType.NORMAL, TriggerType.SCHEDULE);
            WorkflowJobParam n2 = newWorkflowJob(UUIDUtils.shortRandomID(), "hello", JobType.NORMAL, TriggerType.SCHEDULE);

            n1.setChildren(Sets.newHashSet(n2.getId()));

            param.setWorkflow(Lists.newArrayList(n1, n2));
        }
        return param;
    }


    public static PlanParam newFixedDelayAddParam(PlanType planType) {
        PlanParam param = new PlanParam();
        param.setName(UUIDUtils.randomID());
        param.setDescription("测试-固定延迟");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(planType);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_DELAY);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(3));
        param.setScheduleOption(scheduleOptionParam);

        if (PlanType.NORMAL == planType) {
            param.setJob(newJob("hello", JobType.NORMAL));
        } else {
            WorkflowJobParam n1 = newWorkflowJob(UUIDUtils.shortRandomID(), "hello", JobType.NORMAL, TriggerType.SCHEDULE);
            WorkflowJobParam n2 = newWorkflowJob(UUIDUtils.shortRandomID(), "hello", JobType.NORMAL, TriggerType.SCHEDULE);

            n1.setChildren(Sets.newHashSet(n2.getId()));

            param.setWorkflow(Lists.newArrayList(n1, n2));
        }
        return param;
    }

    public static PlanParam newFixedRateReplaceParam(PlanType planType) {
        PlanParam param = new PlanParam();
        param.setName(UUIDUtils.randomID());
        param.setDescription("测试-固定速率-replace");
        param.setTriggerType(TriggerType.SCHEDULE);
        param.setPlanType(planType);

        ScheduleOptionParam scheduleOptionParam = new ScheduleOptionParam();
        scheduleOptionParam.setScheduleType(ScheduleType.FIXED_RATE);
        scheduleOptionParam.setScheduleInterval(Duration.ofSeconds(3));
        param.setScheduleOption(scheduleOptionParam);

        if (PlanType.NORMAL == planType) {
            param.setJob(newJob("hello", JobType.NORMAL));
        } else {
            WorkflowJobParam n1 = newWorkflowJob(UUIDUtils.shortRandomID(), "hello", JobType.NORMAL, TriggerType.SCHEDULE);
            WorkflowJobParam n2 = newWorkflowJob(UUIDUtils.shortRandomID(), "hello", JobType.NORMAL, TriggerType.SCHEDULE);

            n1.setChildren(Sets.newHashSet(n2.getId()));

            param.setWorkflow(Lists.newArrayList(n1, n2));
        }
        return param;
    }

    public static JobParam newJob(String executorName, JobType type) {
        JobParam job = new JobParam();
        job.setType(type);
        job.setRetryOption(RetryOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .build()
        );
        job.setDispatchOption(DispatchOptionParam.builder()
                .loadBalanceType(LoadBalanceType.RANDOM)
                .build()
        );
        Map<String, Object> attr = new HashMap<>();
        attr.put("num", 1);
        job.setAttributes(attr);
        job.setExecutorName(executorName);
        return job;
    }

    public static WorkflowJobParam newWorkflowJob(String id, String executorName, JobType type, TriggerType triggerType) {
        WorkflowJobParam job = new WorkflowJobParam();
        job.setId(id);
        job.setName(executorName + "-" + id);
        job.setDescription(job.getName());
        job.setType(type);
        job.setTriggerType(triggerType);
        job.setRetryOption(RetryOptionParam.builder()
                .retry(2)
                .retryInterval(3)
                .build()
        );
        job.setDispatchOption(DispatchOptionParam.builder()
                .loadBalanceType(LoadBalanceType.RANDOM)
                .build()
        );
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("rid", UUIDUtils.shortRandomID());
        attributes.put("time", TimeUtils.currentInstant().getEpochSecond());
        attributes.put("num", 1);
        attributes.put("node", DAGTest.job("1", new HashSet<>()));
        job.setAttributes(attributes);
        job.setExecutorName(executorName);
        return job;
    }

}
