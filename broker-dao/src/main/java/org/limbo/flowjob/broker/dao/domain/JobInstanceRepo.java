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

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.api.constants.enums.LoadBalanceType;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreatorFactory;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class JobInstanceRepo implements JobInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskCreatorFactory taskCreatorFactory;

    @Setter(onMethod_ = @Inject)
    private JobInfoEntityRepo jobInfoEntityRepo;

    @Override
    public String save(JobInstance jobInstance) {
        JobInstanceEntity entity = toEntity(jobInstance);
        jobInstanceEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public JobInstance get(String jobInstanceId) {
        return jobInstanceEntityRepo.findById(Long.valueOf(jobInstanceId)).map(this::toDO).orElse(null);
    }

    private JobInstanceEntity toEntity(JobInstance jobInstance) {
        JobInstanceEntity jobInstanceEntity = new JobInstanceEntity();
        jobInstanceEntity.setPlanInstanceId(Long.valueOf(jobInstance.getPlanInstanceId()));
        jobInstanceEntity.setJobInfoId(Long.valueOf(jobInstance.getJobId()));
        jobInstanceEntity.setStatus(jobInstance.getStatus().status);
        jobInstanceEntity.setAttributes(jobInstance.getAttributes());
        jobInstanceEntity.setStartAt(jobInstance.getStartAt());
        jobInstanceEntity.setEndAt(jobInstance.getEndAt());
        jobInstanceEntity.setId(Long.valueOf(jobInstance.getJobInstanceId()));
        return jobInstanceEntity;
    }

    private JobInstance toDO(JobInstanceEntity entity) {
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobInstanceId(String.valueOf(entity.getId()));
        jobInstance.setPlanInstanceId(String.valueOf(entity.getPlanInstanceId()));
        jobInstance.setJobId(String.valueOf(entity.getJobInfoId()));
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        jobInstance.setAttributes(entity.getAttributes());
        jobInstance.setTaskCreatorFactory(taskCreatorFactory);
        jobInstance.setTriggerAt(entity.getTriggerAt());

        JobInfoEntity jobInfoEntity = jobInfoEntityRepo.findById(entity.getJobInfoId()).get();
        jobInstance.setDispatchOption(toDispatchOption(jobInfoEntity));
        jobInstance.setExecutorOption(toExecutorOption(jobInfoEntity));
        jobInstance.setType(JobType.parse(jobInfoEntity.getType()));
        jobInstance.setFailHandler(null); // todo
        return jobInstance;
    }

    private DispatchOption toDispatchOption(JobInfoEntity entity) {
        return new DispatchOption(LoadBalanceType.parse(entity.getLoadBalanceType()), entity.getCpuRequirement(), entity.getRamRequirement(), entity.getRetry());
    }

    private ExecutorOption toExecutorOption(JobInfoEntity entity) {
        return new ExecutorOption(entity.getExecutorName(), JobExecuteType.parse(entity.getExecutorType()));
    }

}
