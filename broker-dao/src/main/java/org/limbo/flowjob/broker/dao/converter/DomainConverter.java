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
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基础信息转换 静态方法
 *
 * @author Devil
 * @since 2022/8/11
 */
public class DomainConverter {

    public static PlanInfo toPlanInfo(PlanInfoEntity entity, List<JobInfoEntity> jobInfoEntities) {
        return new PlanInfo(
                entity.getPlanId(),
                entity.getPlanVersion(),
                entity.getDescription(),
                toScheduleOption(entity),
                toJobDag(entity.getJobs(), jobInfoEntities)
        );
    }

    public static ScheduleOption toScheduleOption(PlanInfoEntity entity) {
        return new ScheduleOption(
                ScheduleType.parse(entity.getScheduleType()),
                entity.getScheduleStartAt(),
                Duration.ofMillis(entity.getScheduleDelay()),
                Duration.ofMillis(entity.getScheduleInterval()),
                entity.getScheduleCron(),
                entity.getScheduleCronType()
        );
    }

    /**
     *
     * @param dag 节点关系
     * @return
     */
    public static DAG<JobInfo> toJobDag(String dag, List<JobInfoEntity> jobInfoEntities) {
        List<JobInfo> jobInfos = JacksonUtils.parseObject(dag, new TypeReference<List<JobInfo>>() {
        });
        Map<String, JobInfoEntity> jobInfoEntityMap = jobInfoEntities.stream().collect(Collectors.toMap(JobInfoEntity::getName, entity -> entity, (entity, entity2) -> entity));
        for (JobInfo jobInfo : jobInfos) {
            JobInfoEntity jobInfoEntity = jobInfoEntityMap.get(jobInfo.getName());
            jobInfo.setDescription(jobInfoEntity.getDescription());
            jobInfo.setTriggerType(TriggerType.parse(jobInfoEntity.getTriggerType()));
            jobInfo.setType(JobType.parse(jobInfoEntity.getType()));
            jobInfo.setAttributes(new Attributes(jobInfoEntity.getAttributes()));
            jobInfo.setDispatchOption(JacksonUtils.parseObject(jobInfoEntity.getDispatchOption(), DispatchOption.class));
            jobInfo.setExecutorName(jobInfoEntity.getExecutorName());
            jobInfo.setTerminateWithFail(jobInfoEntity.getTerminateWithFail());
        }
        return new DAG<>(jobInfos);
    }

    public static JobInstance toJobInstance(JobInstanceEntity entity, DAG<JobInfo> dag) {
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobInstanceId(entity.getJobInstanceId());
        jobInstance.setPlanInstanceId(entity.getPlanInstanceId());
        jobInstance.setPlanVersion(entity.getPlanVersion());
        jobInstance.setPlanId(entity.getPlanId());
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
        taskEntity.setJobInstanceId(task.getJobInstanceId());
        taskEntity.setJobId(task.getJobId());
        taskEntity.setPlanId(task.getPlanId());
        taskEntity.setStatus(task.getStatus().status);
        taskEntity.setWorkerId(task.getWorkerId());
        taskEntity.setAttributes(task.getAttributes() == null ? "" : task.getAttributes().toString());
        taskEntity.setStartAt(task.getStartAt());
        taskEntity.setEndAt(task.getEndAt());
        taskEntity.setTaskId(task.getMetaId());
        return taskEntity;
    }

    public static Task toTask(TaskEntity entity, PlanInfoEntityRepo planInfoEntityRepo, JobInfoEntityRepo jobInfoEntityRepo) {
        Task task = new Task();
        task.setTaskId(entity.getTaskId());
        task.setJobInstanceId(entity.getJobInstanceId());
        task.setJobId(entity.getJobId());
        task.setStatus(TaskStatus.parse(entity.getStatus()));
        task.setWorkerId(entity.getWorkerId());
        task.setAttributes(new Attributes(entity.getAttributes()));
        task.setStartAt(entity.getStartAt());
        task.setEndAt(entity.getEndAt());

        // job
        PlanInfoEntity planInfo = planInfoEntityRepo.findByPlanIdAndPlanVersion(task.getPlanId(), task.getPlanVersion());
        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findByPlanInfoId(planInfo.getPlanInfoId());
        DAG<JobInfo> jobInfoDAG = DomainConverter.toJobDag(planInfo.getJobs(), jobInfoEntities);
        JobInfo jobInfo = jobInfoDAG.getNode(entity.getJobId());
        task.setDispatchOption(jobInfo.getDispatchOption());
        task.setExecutorName(jobInfo.getExecutorName());
        return task;
    }

    public static JobInstanceEntity toJobInstanceEntity(JobInstance jobInstance) {
        JobInstanceEntity entity = new JobInstanceEntity();
        entity.setJobInstanceId(jobInstance.getJobInstanceId());
        entity.setPlanInstanceId(jobInstance.getPlanInstanceId());
        entity.setPlanId(jobInstance.getPlanId());
        entity.setPlanVersion(jobInstance.getPlanVersion());
        entity.setJobId(jobInstance.getJobId());
        entity.setStatus(jobInstance.getStatus().status);
        entity.setAttributes(JacksonUtils.toJSONString(jobInstance.getAttributes(), JacksonUtils.DEFAULT_NONE_OBJECT));
        entity.setTriggerAt(jobInstance.getTriggerAt());
        entity.setStartAt(jobInstance.getStartAt());
        entity.setEndAt(jobInstance.getEndAt());
        return entity;
    }
}
