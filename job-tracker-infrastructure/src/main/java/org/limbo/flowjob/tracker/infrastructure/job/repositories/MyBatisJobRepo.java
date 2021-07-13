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
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobMapper;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

    /**
     * {@inheritDoc}
     * @param job 作业数据
     */
    @Override
    public void addOrUpdateJob(Job job) {
        JobPO po = converter.convert(job);
        Objects.requireNonNull(po);

        int effected = mapper.insertOrUpdate(po);
        if (effected <= 0) {
            throw new IllegalStateException(String.format("Update job error, effected %s rows", effected));
        }
    }


    /**
     * {@inheritDoc}
     * @param jobId jobId
     * @return
     */
    @Override
    public Job getJob(String jobId) {
        JobPO po = mapper.selectById(jobId);
        return converter.reverse().convert(po);
    }

}
