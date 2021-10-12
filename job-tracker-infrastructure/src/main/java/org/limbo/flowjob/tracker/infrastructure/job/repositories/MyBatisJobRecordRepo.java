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
import org.limbo.flowjob.tracker.core.job.context.JobRecord;
import org.limbo.flowjob.tracker.core.job.context.JobRecordRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobRecordMapper;
import org.limbo.flowjob.tracker.dao.po.JobRecordPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobRecordPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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
    public JobRecord get(String planId, Long planRecordId, Integer planInstanceId, String jobId) {
        JobRecordPO po = jobRecordMapper.selectOne(Wrappers.<JobRecordPO>lambdaQuery()
                .eq(JobRecordPO::getPlanId, planId)
                .eq(JobRecordPO::getPlanRecordId, planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, planInstanceId)
                .eq(JobRecordPO::getJobId, jobId)
        );
        return convert.reverse().convert(po);
    }

    @Override
    public void executing(String planId, Long planRecordId, Integer planInstanceId, String jobId) {
        jobRecordMapper.update(null, Wrappers.<JobRecordPO>lambdaUpdate()
                .set(JobRecordPO::getState, JobScheduleStatus.EXECUTING.status)
                .eq(JobRecordPO::getPlanId, planId)
                .eq(JobRecordPO::getPlanRecordId, planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, planInstanceId)
                .eq(JobRecordPO::getJobId, jobId)
                .eq(JobRecordPO::getState, JobScheduleStatus.SCHEDULING.status)
        );
    }

    @Override
    public void end(String planId, Long planRecordId, Integer planInstanceId, String jobId, JobScheduleStatus state) {
        jobRecordMapper.update(null, Wrappers.<JobRecordPO>lambdaUpdate()
                .set(JobRecordPO::getState, state.status)
                .eq(JobRecordPO::getPlanId, planId)
                .eq(JobRecordPO::getPlanRecordId, planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, planInstanceId)
                .eq(JobRecordPO::getJobId, jobId)
                .eq(JobRecordPO::getState, JobScheduleStatus.EXECUTING.status)
        );
    }

    @Override
    public List<JobRecord> getRecords(String planId, Long planRecordId, Integer planInstanceId, List<String> jobIds) {
        List<JobRecord> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(jobIds)) {
            return result;
        }
        List<JobRecordPO> pos = jobRecordMapper.selectList(Wrappers.<JobRecordPO>lambdaQuery()
                .eq(JobRecordPO::getPlanId, planId)
                .eq(JobRecordPO::getPlanRecordId, planRecordId)
                .eq(JobRecordPO::getPlanInstanceId, planInstanceId)
                .in(JobRecordPO::getJobId, jobIds)
        );
        if (CollectionUtils.isEmpty(pos)) {
            return result;
        }
        for (JobRecordPO po : pos) {
            result.add(convert.reverse().convert(po));
        }
        return result;
    }
}
