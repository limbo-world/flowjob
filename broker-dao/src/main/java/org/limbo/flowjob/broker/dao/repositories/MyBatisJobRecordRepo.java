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
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstanceContext;
import org.limbo.flowjob.broker.core.plan.job.context.JobRecord;
import org.limbo.flowjob.broker.core.repositories.JobRecordRepository;
import org.limbo.flowjob.broker.dao.converter.JobRecordPoConverter;
import org.limbo.flowjob.broker.dao.mybatis.JobRecordMapper;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisJobRecordRepo implements JobRecordRepository {

    @Autowired
    private JobRecordPoConverter convert;

    @Autowired
    private JobRecordMapper jobRecordMapper;


    @Override
    public void add(JobRecord record) {
        JobInstanceEntity po = this.convert.convert(record);
        jobRecordMapper.insert(po);
    }


    @Override
    public JobRecord get(JobRecord.ID jobRecordId) {
        JobInstanceEntity po = jobRecordMapper.selectOne(Wrappers.<JobInstanceEntity>lambdaQuery()
                .eq(JobInstanceEntity::getPlanId, jobRecordId.planId)
                .eq(JobInstanceEntity::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobInstanceEntity::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobInstanceEntity::getJobId, jobRecordId.jobId)
        );
        return convert.reverse().convert(po);
    }


    /**
     * {@inheritDoc}
     * @param jobRecordId 待更新的作业执行记录ID
     * @return
     */
    @Override
    public boolean execute(JobRecord.ID jobRecordId) {
        return jobRecordMapper.update(null, Wrappers.<JobInstanceEntity>lambdaUpdate()
                .set(JobInstanceEntity::getState, JobScheduleStatus.EXECUTING.status)
                .eq(JobInstanceEntity::getPlanId, jobRecordId.planId)
                .eq(JobInstanceEntity::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobInstanceEntity::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobInstanceEntity::getJobId, jobRecordId.jobId)
                .eq(JobInstanceEntity::getState, JobScheduleStatus.SCHEDULING.status)
        ) > 0;
    }


    @Override
    public void end(JobRecord.ID jobRecordId, JobScheduleStatus state) {
        jobRecordMapper.update(null, Wrappers.<JobInstanceEntity>lambdaUpdate()
                .set(JobInstanceEntity::getState, state.status)
                .eq(JobInstanceEntity::getPlanId, jobRecordId.planId)
                .eq(JobInstanceEntity::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobInstanceEntity::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobInstanceEntity::getJobId, jobRecordId.jobId)
                .eq(JobInstanceEntity::getState, JobScheduleStatus.EXECUTING.status)
        );
    }


    @Override
    public List<JobRecord> getRecords(PlanInstanceContext.ID planInstanceId, Collection<String> jobIds) {
        return jobRecordMapper.selectList(Wrappers.<JobInstanceEntity>lambdaQuery()
                .eq(JobInstanceEntity::getPlanId, planInstanceId.planId)
                .eq(JobInstanceEntity::getPlanRecordId, planInstanceId.planRecordId)
                .eq(JobInstanceEntity::getPlanInstanceId, planInstanceId.planInstanceId)
                .in(JobInstanceEntity::getJobId, jobIds)
        )
                .stream()
                .map(po -> convert.reverse().convert(po))
                .collect(Collectors.toList());
    }
}
