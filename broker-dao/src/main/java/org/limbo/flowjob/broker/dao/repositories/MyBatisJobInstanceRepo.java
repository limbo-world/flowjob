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

package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.JobInstanceConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.mybatis.JobInstanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisJobInstanceRepo implements JobInstanceRepository {

    @Autowired
    private JobInstanceConverter convert;

    @Autowired
    private JobInstanceMapper jobInstanceMapper;

    @Setter(onMethod_ = @Inject)
    private IDRepo idRepo;


    @Override
    public String add(JobInstance jobInstance) {
        String jobInstanceId = idRepo.createJobInstanceId();
        jobInstance.setJobInstanceId(jobInstanceId);

        JobInstanceEntity po = this.convert.convert(jobInstance);
        jobInstanceMapper.insert(po);
        return jobInstanceId;
    }


    @Override
    public JobInstance get(String jobInstanceId) {
        JobInstanceEntity po = jobInstanceMapper.selectOne(Wrappers.<JobInstanceEntity>lambdaQuery()
                .eq(JobInstanceEntity::getJobInstanceId, jobInstanceId)
        );
        return convert.reverse().convert(po);
    }


    /**
     * {@inheritDoc}
     * @param jobInstanceId 待更新的作业执行记录ID
     * @return
     */
    @Override
    public boolean execute(String jobInstanceId) {
        return jobInstanceMapper.update(null, Wrappers.<JobInstanceEntity>lambdaUpdate()
                .set(JobInstanceEntity::getState, JobScheduleStatus.EXECUTING.status)
                .eq(JobInstanceEntity::getJobInstanceId, jobInstanceId)
                .eq(JobInstanceEntity::getState, JobScheduleStatus.SCHEDULING.status)
        ) > 0;
    }


    @Override
    public boolean end(String jobInstanceId, JobScheduleStatus state) {
        return jobInstanceMapper.update(null, Wrappers.<JobInstanceEntity>lambdaUpdate()
                .set(JobInstanceEntity::getState, state.status)
                .eq(JobInstanceEntity::getJobInstanceId, jobInstanceId)
                .eq(JobInstanceEntity::getState, JobScheduleStatus.EXECUTING.status)
        ) > 0;
    }


    @Override
    public List<JobInstance> getInstances(String planInstanceId, Collection<String> jobIds) {
        return jobInstanceMapper.selectList(Wrappers.<JobInstanceEntity>lambdaQuery()
                .in(JobInstanceEntity::getJobInfoId, jobIds)
                .in(JobInstanceEntity::getPlanInstanceId, planInstanceId)
        )
                .stream()
                .map(po -> convert.reverse().convert(po))
                .collect(Collectors.toList());
    }
}
