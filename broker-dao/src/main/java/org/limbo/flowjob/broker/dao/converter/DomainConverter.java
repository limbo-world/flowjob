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
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
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

    public static JobInstance toJobInstance(JobInstanceEntity entity, DAG<JobInfo> dag) {
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobInstanceId(entity.getId().toString());
        jobInstance.setPlanInstanceId(entity.getPlanInstanceId().toString());
        jobInstance.setPlanVersion(entity.getPlanInfoId().toString());
        jobInstance.setPlanId(entity.getPlanId().toString());
        jobInstance.setJobId(entity.getJobId());
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        jobInstance.setAttributes(new Attributes(entity.getAttributes()));
        jobInstance.setTriggerAt(entity.getTriggerAt());

        JobInfo jobInfo = dag.getNode(entity.getJobId());
        jobInstance.setDispatchOption(jobInfo.getDispatchOption());
        jobInstance.setExecutorName(jobInfo.getExecutorName());
        jobInstance.setType(jobInfo.getType());
        jobInstance.setTerminateWithFail(jobInfo.isTerminateWithFail());
        return jobInstance;
    }

    public static TaskEntity toTaskEntity(Task task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setJobInstanceId(Long.valueOf(task.getJobInstanceId()));
        taskEntity.setJobId(task.getJobId());
        taskEntity.setPlanId(Long.parseLong(task.getPlanId()));
        taskEntity.setStatus(task.getStatus().status);
        taskEntity.setWorkerId(StringUtils.isBlank(task.getWorkerId()) ? 0L : Long.parseLong(task.getWorkerId()));
        taskEntity.setAttributes(task.getAttributes() == null ? "" : task.getAttributes().toString());
        taskEntity.setStartAt(task.getStartAt());
        taskEntity.setEndAt(task.getEndAt());
        taskEntity.setId(task.getTaskId() == null ? null : Long.valueOf(task.getTaskId()));
        return taskEntity;
    }

    public static Task toTask(TaskEntity entity, PlanInfoEntityRepo planInfoEntityRepo) {
        Task task = new Task();
        task.setTaskId(entity.getId().toString());
        task.setJobInstanceId(entity.getJobInstanceId().toString());
        task.setJobId(entity.getJobId().toString());
        task.setStatus(TaskStatus.parse(entity.getStatus()));
        task.setWorkerId(entity.getWorkerId().toString());
        task.setAttributes(new Attributes(entity.getAttributes()));
        task.setStartAt(entity.getStartAt());
        task.setEndAt(entity.getEndAt());

        // job
        PlanInfoEntity planInfo = planInfoEntityRepo.findById(Long.valueOf(task.getPlanVersion())).get();
        DAG<JobInfo> jobInfoDAG = DomainConverter.toJobDag(planInfo.getJobs());
        JobInfo jobInfo = jobInfoDAG.getNode(entity.getJobId().toString());
        task.setDispatchOption(jobInfo.getDispatchOption());
        task.setExecutorName(jobInfo.getExecutorName());
        return task;
    }

    public static JobInstanceEntity toJobInstanceEntity(JobInstance jobInstance) {
        JobInstanceEntity entity = new JobInstanceEntity();
        entity.setPlanInstanceId(Long.valueOf(jobInstance.getPlanInstanceId()));
        entity.setPlanId(Long.valueOf(jobInstance.getPlanId()));
        entity.setPlanInfoId(Long.valueOf(jobInstance.getPlanVersion()));
        entity.setJobId(jobInstance.getJobId());
        entity.setStatus(jobInstance.getStatus().status);
        entity.setAttributes(jobInstance.getAttributes() == null ? "{}" : jobInstance.getAttributes().toString());
        entity.setTriggerAt(jobInstance.getTriggerAt());
        entity.setStartAt(jobInstance.getStartAt());
        entity.setEndAt(jobInstance.getEndAt());
        if (jobInstance.getJobInstanceId() != null) {
            entity.setId(Long.valueOf(jobInstance.getJobInstanceId()));
        }
        return entity;
    }
}
