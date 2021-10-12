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
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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
    public Integer createId(String planId, Long planRecordId, Integer planInstanceId, String jobId) {
        Integer recentlyIdForUpdate = jobInstanceMapper.getRecentlyIdForUpdate(planId, planRecordId, planInstanceId, jobId);
        return recentlyIdForUpdate == null ? 1 : recentlyIdForUpdate + 1;
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
    public void executing(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId) {
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
    public void end(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, JobScheduleStatus state) {
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

    @Override
    public List<JobInstance> listByRecord(String planId, Long planRecordId, Integer planInstanceId, String jobId) {
        List<JobInstance> result = new ArrayList<>();
        List<JobInstancePO> pos = jobInstanceMapper.selectList(Wrappers.<JobInstancePO>lambdaQuery()
                .eq(JobInstancePO::getPlanId, planId)
                .eq(JobInstancePO::getPlanRecordId, planRecordId)
                .eq(JobInstancePO::getPlanInstanceId, planInstanceId)
                .eq(JobInstancePO::getJobId, jobId)
        );
        if (CollectionUtils.isEmpty(pos)) {
            return result;
        }
        for (JobInstancePO po : pos) {
            result.add(convert.reverse().convert(po));
        }
        return result;
    }

}
