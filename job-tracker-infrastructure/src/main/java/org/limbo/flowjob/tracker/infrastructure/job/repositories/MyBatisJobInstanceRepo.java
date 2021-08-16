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
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    /**
     * {@inheritDoc}
     * @param instance 作业执行上下文
     */
    @Override
    public void addInstance(JobInstance instance) {
        JobInstancePO po = jobInstancePoConverter.convert(instance);
        jobInstanceMapper.insert(po);
    }

    /**
     * {@inheritDoc}
     * @param instance 作业执行上下文
     */
    @Override
    public void updateInstance(JobInstance instance) {
        JobInstancePO po = jobInstancePoConverter.convert(instance);
        jobInstanceMapper.update(po, Wrappers.<JobInstancePO>lambdaUpdate()
                .eq(JobInstancePO::getPlanId, po.getPlanId())
                .eq(JobInstancePO::getPlanInstanceId, po.getPlanInstanceId())
                .eq(JobInstancePO::getJobId, po.getJobId())
        );
    }

    @Override
    public void compareAndSwapInstanceState(String planId, Long planInstanceId, String jobId,
                                     JobScheduleStatus oldState, JobScheduleStatus newState) {
        jobInstanceMapper.update(null, Wrappers.<JobInstancePO>lambdaUpdate()
                .set(JobInstancePO::getState, newState.status)
                .eq(JobInstancePO::getPlanId, planId)
                .eq(JobInstancePO::getPlanInstanceId, planInstanceId)
                .eq(JobInstancePO::getJobId, jobId)
                .eq(JobInstancePO::getState, oldState.status)
        );
    }


    /**
     * {@inheritDoc}
     * @param planId 作业ID
     * @param planInstanceId 实例ID
     * @param jobId 作业ID
     * @return
     */
    @Override
    public JobInstance getInstance(String planId, Long planInstanceId, String jobId) {
        JobInstancePO po = jobInstanceMapper.selectOne(Wrappers.<JobInstancePO>lambdaQuery()
                .eq(JobInstancePO::getPlanId, planId)
                .eq(JobInstancePO::getPlanInstanceId, planInstanceId)
                .eq(JobInstancePO::getJobId, jobId)
        );
        return jobInstancePoConverter.reverse().convert(po);
    }

    /**
     * {@inheritDoc}
     * @param jobId 作业ID
     * @return
     */
    @Override
    public JobInstance getLatestInstance(String jobId) {
//        JobExecuteRecordPO po = mapper.selectOne(Wrappers.<JobExecuteRecordPO>lambdaQuery()
//                .eq(JobExecuteRecordPO::getJobId, jobId)
//                .orderByDesc(JobExecuteRecordPO::getCreatedAt)
//                .last("limit 1"));
//        return converter.reverse().convert(po);
        return null;
    }

}
