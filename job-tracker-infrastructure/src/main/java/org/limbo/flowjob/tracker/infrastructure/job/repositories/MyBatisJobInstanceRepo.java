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
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.context.JobRecord;
import org.limbo.flowjob.tracker.dao.mybatis.JobInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisJobInstanceRepo implements JobInstanceRepository {

    @Autowired
    private JobInstancePoConverter convert;

    @Autowired
    private JobInstanceMapper jobInstanceMapper;


    @Override
    public JobInstance.ID createId(JobRecord.ID jobRecordId) {
        Integer recentlyIdForUpdate = jobInstanceMapper.getRecentlyIdForUpdate(
                jobRecordId.planId,
                jobRecordId.planRecordId,
                jobRecordId.planInstanceId,
                jobRecordId.jobId
        );
        int jobInstanceId = recentlyIdForUpdate == null ? 1 : recentlyIdForUpdate + 1;
        return new JobInstance.ID(
                jobRecordId.planId,
                jobRecordId.planRecordId,
                jobRecordId.planInstanceId,
                jobRecordId.jobId,
                jobInstanceId
        );
    }


    /**
     * {@inheritDoc}
     *
     * @param instance 作业执行上下文
     */
    @Override
    public void add(JobInstance instance) {
        JobInstancePO po = convert.convert(instance);
        jobInstanceMapper.insert(po);
    }


    @Override
    public void end(JobInstance.ID jobInstanceId, JobScheduleStatus state) {
        jobInstanceMapper.update(null, Wrappers.<JobInstancePO>lambdaUpdate()
                .set(JobInstancePO::getState, state.status)
                .set(JobInstancePO::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(JobInstancePO::getPlanId, jobInstanceId.planId)
                .eq(JobInstancePO::getPlanRecordId, jobInstanceId.planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, jobInstanceId.planInstanceId)
                .eq(JobInstancePO::getJobId, jobInstanceId.jobId)
                .eq(JobInstancePO::getJobInstanceId, jobInstanceId.jobInstanceId)
                .eq(JobInstancePO::getState, JobScheduleStatus.EXECUTING.status)
        );
    }


    /**
     * {@inheritDoc}
     * @param jobInstanceId
     * @return
     */
    @Override
    public boolean execute(JobInstance.ID jobInstanceId) {
        return jobInstanceMapper.update(null, Wrappers.<JobInstancePO>lambdaUpdate()
                .set(JobInstancePO::getState, JobScheduleStatus.EXECUTING.status)
                .eq(JobInstancePO::getPlanId, jobInstanceId.planId)
                .eq(JobInstancePO::getPlanRecordId, jobInstanceId.planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, jobInstanceId.planInstanceId)
                .eq(JobInstancePO::getJobId, jobInstanceId.jobId)
                .eq(JobInstancePO::getJobInstanceId, jobInstanceId.jobInstanceId)
                .eq(JobInstancePO::getState, JobScheduleStatus.SCHEDULING.status)
        ) > 0;
    }


    /**
     * {@inheritDoc}
     * @param jobInstanceId 作业执行实例ID
     * @return
     */
    @Override
    public JobInstance get(JobInstance.ID jobInstanceId) {
        JobInstancePO po = jobInstanceMapper.selectOne(Wrappers
                .<JobInstancePO>lambdaQuery()
                .eq(JobInstancePO::getPlanId, jobInstanceId.planId)
                .eq(JobInstancePO::getPlanRecordId, jobInstanceId.planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, jobInstanceId.planInstanceId)
                .eq(JobInstancePO::getJobId, jobInstanceId.jobId)
                .eq(JobInstancePO::getJobInstanceId, jobInstanceId.jobInstanceId)
        );

        return convert.reverse().convert(po);
    }


    /**
     * {@inheritDoc}
     * @param jobRecordId 作业执行记录ID
     * @return
     */
    @Override
    public List<JobInstance> listByRecord(JobRecord.ID jobRecordId) {
        return jobInstanceMapper.selectList(Wrappers
                .<JobInstancePO>lambdaQuery()
                .eq(JobInstancePO::getPlanId, jobRecordId.planId)
                .eq(JobInstancePO::getPlanRecordId, jobRecordId.planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, jobRecordId.planInstanceId)
                .eq(JobInstancePO::getJobId, jobRecordId.jobId)
        )
                .stream()
                .map(po -> convert.reverse().convert(po))
                .collect(Collectors.toList());
    }

}
