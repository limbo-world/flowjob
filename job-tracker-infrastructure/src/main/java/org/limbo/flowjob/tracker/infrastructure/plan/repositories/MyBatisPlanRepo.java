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

package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobMapper;
import org.limbo.flowjob.tracker.dao.mybatis.PlanMapper;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobPoConverter;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanPoConverter;
import org.limbo.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Repository
public class MyBatisPlanRepo implements PlanRepository {

    @Autowired
    private PlanMapper mapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private PlanPoConverter converter;

    @Autowired
    private JobPoConverter jobPoConverter;

    /**
     * {@inheritDoc}
     * @param plan 计划plan
     * @return
     */
    @Override
    public String addOrUpdatePlan(Plan plan) {
        // ID未设置则生成一个
        if (plan.getPlanId() == null) {
            plan.setPlanId(UUIDUtils.randomID());
        }

        // 更新plan
        PlanPO po = converter.convert(plan);
        if (mapper.insertOrUpdate(po) < 1) {
            throw new IllegalStateException("Update plan error, effected 0 rows");
        }

        // 更新Job，先删后增
        List<JobPO> jobs = plan.getJobs().stream()
                .peek(job -> job.setPlanId(plan.getPlanId()))
                .map(jobPoConverter::convert)
                .collect(Collectors.toList());
        jobMapper.delete(Wrappers.<JobPO>lambdaQuery()
                .eq(JobPO::getPlanId, plan.getPlanId()));
        jobMapper.batchInsert(jobs);

        return plan.getPlanId();
    }


    /**
     * {@inheritDoc}
     * @param planId 计划ID
     * @return
     */
    @Override
    public Plan getPlan(String planId) {
        Plan plan = converter.reverse().convert(mapper.selectById(planId));

        if (plan != null) {
            List<Job> jobs = jobMapper.selectList(Wrappers.<JobPO>lambdaQuery()
                    .eq(JobPO::getPlanId, plan.getPlanId()))
                    .stream()
                    .map(jpo -> jobPoConverter.reverse().convert(jpo))
                    .collect(Collectors.toList());
            plan.setJobs(jobs);
        }

        return plan;
    }


    /**
     * TODO
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<Plan> listSchedulablePlans() {
        throw new UnsupportedOperationException();
    }
}
