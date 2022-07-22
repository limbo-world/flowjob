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
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.JobInstanceConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class JobInstanceRepo implements JobInstanceRepository {

    @Autowired
    private JobInstanceConverter convert;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;


    @Override
    public String add(JobInstance jobInstance) {
        JobInstanceEntity entity = this.convert.convert(jobInstance);
        jobInstanceEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }


    @Override
    public JobInstance get(String jobInstanceId) {
        return jobInstanceEntityRepo.findById(Long.valueOf(jobInstanceId)).map(entity -> convert.reverse().convert(entity)).orElse(null);
    }


    /**
     * {@inheritDoc}
     *
     * @param instance 作业实例
     * @return
     */
    @Override
    public boolean dispatched(JobInstance instance) {
        return jobInstanceEntityRepo.updateStatus(Long.valueOf(instance.getJobInstanceId()),
                JobScheduleStatus.SCHEDULING.status,
                JobScheduleStatus.EXECUTING.status
        ) > 0;
    }


    /**
     * {@inheritDoc}
     *
     * @param instance 作业实例
     * @return
     */
    @Override
    public boolean dispatchFailed(JobInstance instance) {
        return jobInstanceEntityRepo.updateStatus(Long.valueOf(instance.getJobInstanceId()),
                JobScheduleStatus.SCHEDULING.status,
                JobScheduleStatus.FAILED.status
        ) > 0;
    }


    /**
     * {@inheritDoc}
     *
     * @param instance 作业实例
     * @return
     */
    @Override
    public boolean executeSucceed(JobInstance instance) {
        return jobInstanceEntityRepo.updateStatus(
                Long.valueOf(instance.getJobInstanceId()),
                JobScheduleStatus.EXECUTING.status,
                JobScheduleStatus.SUCCEED.status
        ) > 0;
    }


    /**
     * {@inheritDoc}
     *
     * @param instance 作业实例
     * @return
     */
    @Override
    public boolean executeFailed(JobInstance instance) {
        return jobInstanceEntityRepo.updateStatus(
                Long.valueOf(instance.getJobInstanceId()),
                JobScheduleStatus.EXECUTING.status,
                JobScheduleStatus.FAILED.status
        ) > 0;
    }

    @Override
    public List<JobInstance> listInstances(String planInstanceId, Collection<String> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Collections.emptyList();
        }
        return jobInstanceEntityRepo.findByPlanInstanceIdAndJobInfoIdIn(
                        Long.valueOf(planInstanceId),
                        jobIds.stream().map(Long::valueOf).collect(Collectors.toList())
                ).stream()
                .map(po -> convert.reverse().convert(po))
                .collect(Collectors.toList());
    }
}
