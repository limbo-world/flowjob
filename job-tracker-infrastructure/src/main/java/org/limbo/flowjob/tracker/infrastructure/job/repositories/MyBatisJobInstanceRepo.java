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
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.dao.mybatis.JobInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisJobInstanceRepo implements JobInstanceRepository {

    @Autowired
    private JobInstancePoConverter jobInstancePoConverter;

    @Autowired
    private JobInstanceMapper jobInstanceMapper;

    @Override
    public Long createId() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @param instance 作业执行上下文
     */
    @Override
    public void add(JobInstance instance) {
        JobInstancePO po = jobInstancePoConverter.convert(instance);
        jobInstanceMapper.insert(po);
    }

    @Override
    public void executing(String planId, Long planRecordId, Long planInstanceId, String jobId, Long jobInstanceId) {
        jobInstanceMapper.update(null, Wrappers.<JobInstancePO>lambdaUpdate()
                .set(JobInstancePO::getState, JobScheduleStatus.EXECUTING.status)
                .eq(JobInstancePO::getPlanId, planId)
                .eq(JobInstancePO::getPlanRecordId, planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, planInstanceId)
                .eq(JobInstancePO::getJobId, jobId)
                .eq(JobInstancePO::getJobInstanceId, jobInstanceId)
                .eq(JobInstancePO::getState, JobScheduleStatus.SCHEDULING.status)
        );
    }

    @Override
    public void end(String planId, Long planRecordId, Long planInstanceId, String jobId, Long jobInstanceId, JobScheduleStatus state) {
        jobInstanceMapper.update(null, Wrappers.<JobInstancePO>lambdaUpdate()
                .set(JobInstancePO::getState, state.status)
                .set(JobInstancePO::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(JobInstancePO::getPlanId, planId)
                .eq(JobInstancePO::getPlanRecordId, planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, planInstanceId)
                .eq(JobInstancePO::getJobId, jobId)
                .eq(JobInstancePO::getJobInstanceId, jobInstanceId)
                .eq(JobInstancePO::getState, JobScheduleStatus.EXECUTING.status)
        );
    }


    /**
     * {@inheritDoc}
     *
     * @param planId         作业ID
     * @param planInstanceId 实例ID
     * @param jobId          作业ID
     * @return
     */
    @Override
    public Task get(String planId, Long planInstanceId, String jobId) {
        JobInstancePO po = jobInstanceMapper.selectOne(Wrappers.<JobInstancePO>lambdaQuery()
                .eq(JobInstancePO::getPlanId, planId)
                .eq(JobInstancePO::getPlanInstanceId, planInstanceId)
                .eq(JobInstancePO::getJobId, jobId)
        );
        // todo
        return null;
//        return jobInstancePoConverter.reverse().convert(po);
    }

    @Override
    public List<JobInstance> list(String planId, Long planRecordId, Long planInstanceId, String jobId) {
        return null;
    }

}
