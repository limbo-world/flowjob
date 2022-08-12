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

package org.limbo.flowjob.broker.dao.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.job.JobInfo;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreatorFactory;
import org.limbo.flowjob.broker.core.plan.job.dag.DAG;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.time.Duration;
import java.util.List;

/**
 * 基础信息转换 静态方法
 *
 * @author Devil
 * @since 2022/8/11
 */
public class DomainConverter {

    public static PlanInfo toPlanInfo(PlanInfoEntity entity) {
        return new PlanInfo(
                String.valueOf(entity.getPlanId()),
                String.valueOf(entity.getId()),
                entity.getDescription(),
                toScheduleOption(entity),
                toJobDag(entity.getJobs())
        );
    }

    public static ScheduleOption toScheduleOption(PlanInfoEntity entity) {
        return new ScheduleOption(
                ScheduleType.parse(entity.getScheduleType()),
                TriggerType.parse(entity.getTriggerType()),
                entity.getScheduleStartAt(),
                Duration.ofMillis(entity.getScheduleDelay()),
                Duration.ofMillis(entity.getScheduleInterval()),
                entity.getScheduleCron(),
                entity.getScheduleCronType()
        );
    }

    public static DAG<JobInfo> toJobDag(String json) {
        return new DAG<>(JacksonUtils.parseObject(json, new TypeReference<List<JobInfo>>() {
        }));
    }

    public static JobInstance toJobInstance(JobInstanceEntity entity, TaskCreatorFactory taskCreatorFactory, PlanInfoEntity planInfoEntity) {
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobInstanceId(entity.getId().toString());
        jobInstance.setPlanInstanceId(entity.getPlanInstanceId().toString());
        jobInstance.setPlanVersion(entity.getPlanInfoId().toString());
        jobInstance.setJobId(entity.getJobId());
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        jobInstance.setAttributes(entity.getAttributes());
        jobInstance.setTaskCreatorFactory(taskCreatorFactory);
        jobInstance.setTriggerAt(entity.getTriggerAt());

        DAG<JobInfo> dag = toJobDag(planInfoEntity.getJobs());
        JobInfo jobInfo = dag.getNode(entity.getJobId());
        jobInstance.setDispatchOption(jobInfo.getDispatchOption());
        jobInstance.setExecutorOption(jobInfo.getExecutorOption());
        jobInstance.setType(jobInfo.getType());
        jobInstance.setFailHandler(null); // todo
        return jobInstance;
    }
}
