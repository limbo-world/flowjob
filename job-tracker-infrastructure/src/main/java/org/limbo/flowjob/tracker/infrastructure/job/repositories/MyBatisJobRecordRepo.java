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

package org.limbo.flowjob.tracker.infrastructure.job.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.core.job.context.JobRecord;
import org.limbo.flowjob.tracker.core.job.context.JobRecordRepository;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.dao.mybatis.JobRecordMapper;
import org.limbo.flowjob.broker.dao.po.JobRecordPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobRecordPoConverter;
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
        JobRecordPO po = this.convert.convert(record);
        jobRecordMapper.insert(po);
    }


    @Override
    public JobRecord get(JobRecord.ID jobRecordId) {
        JobRecordPO po = jobRecordMapper.selectOne(Wrappers.<JobRecordPO>lambdaQuery()
                .eq(JobRecordPO::getPlanId, jobRecordId.planId)
                .eq(JobRecordPO::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobRecordPO::getJobId, jobRecordId.jobId)
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
        return jobRecordMapper.update(null, Wrappers.<JobRecordPO>lambdaUpdate()
                .set(JobRecordPO::getState, JobScheduleStatus.EXECUTING.status)
                .eq(JobRecordPO::getPlanId, jobRecordId.planId)
                .eq(JobRecordPO::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobRecordPO::getJobId, jobRecordId.jobId)
                .eq(JobRecordPO::getState, JobScheduleStatus.SCHEDULING.status)
        ) > 0;
    }


    @Override
    public void end(JobRecord.ID jobRecordId, JobScheduleStatus state) {
        jobRecordMapper.update(null, Wrappers.<JobRecordPO>lambdaUpdate()
                .set(JobRecordPO::getState, state.status)
                .eq(JobRecordPO::getPlanId, jobRecordId.planId)
                .eq(JobRecordPO::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobRecordPO::getJobId, jobRecordId.jobId)
                .eq(JobRecordPO::getState, JobScheduleStatus.EXECUTING.status)
        );
    }


    @Override
    public List<JobRecord> getRecords(PlanInstance.ID planInstanceId, Collection<String> jobIds) {
        return jobRecordMapper.selectList(Wrappers.<JobRecordPO>lambdaQuery()
                .eq(JobRecordPO::getPlanId, planInstanceId.planId)
                .eq(JobRecordPO::getPlanRecordId, planInstanceId.planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, planInstanceId.planInstanceId)
                .in(JobRecordPO::getJobId, jobIds)
        )
                .stream()
                .map(po -> convert.reverse().convert(po))
                .collect(Collectors.toList());
    }
}
