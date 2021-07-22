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
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobMapper;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job的repo，领域层使用。MyBatisPlus实现
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Repository
public class MyBatisJobRepo implements JobRepository {

    /**
     * MyBatis Mapper
     */
    @Autowired
    private JobMapper mapper;

    /**
     * DO 与 PO 的转换器
     */
    @Autowired
    private JobPoConverter converter;


    @Override
    public void batchInsert(List<Job> jobs) {
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        mapper.batchInsert(jobs.stream().map(converter::convert).collect(Collectors.toList()));
    }

    @Override
    public List<Job> getUsedJobsByPlan(String planId) {
        List<JobPO> jobPOS = mapper.selectList(Wrappers.<JobPO>lambdaQuery()
                .eq(JobPO::getPlanId, planId)
                .eq(JobPO::getIsDeleted, false)
        );
        if (CollectionUtils.isEmpty(jobPOS)) {
            return new ArrayList<>();
        }
        return jobPOS.stream().map(job -> converter.reverse().convert(job)).collect(Collectors.toList());
    }

    @Override
    public void deleteUsedJobsByPlan(String planId) {
        mapper.delete(Wrappers.<JobPO>lambdaQuery()
                .eq(JobPO::getPlanId, planId)
                .eq(JobPO::getIsDeleted, false)
        );
    }


    /**
     * {@inheritDoc}
     *
     * @param jobId jobId
     * @return
     */
    @Override
    public Job getJob(String jobId) {
        JobPO po = mapper.selectById(jobId);
        return converter.reverse().convert(po);
    }

}
