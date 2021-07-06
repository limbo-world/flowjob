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
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobExecuteRecordMapper;
import org.limbo.flowjob.tracker.dao.po.JobExecuteRecordPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobExecuteRecordPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisJobContextRepo implements JobContextRepository {

    /**
     * MyBatisMapper
     */
    @Autowired
    private JobExecuteRecordMapper mapper;

    /**
     * {@link JobContext}和{@link JobExecuteRecordPO}的转换器
     */
    @Autowired
    private JobExecuteRecordPoConverter converter;

    /**
     * {@inheritDoc}
     * @param context 作业执行上下文
     */
    @Override
    public void addContext(JobContext context) {
        JobExecuteRecordPO po = converter.convert(context);
        mapper.insert(po);
    }

    /**
     * {@inheritDoc}
     * @param context 作业执行上下文
     */
    @Override
    public void updateContext(JobContext context) {

        JobExecuteRecordPO po = converter.convert(context);
        Objects.requireNonNull(po);

        mapper.update(po, Wrappers.<JobExecuteRecordPO>lambdaUpdate()
                .eq(JobExecuteRecordPO::getJobId, po.getJobId())
                .eq(JobExecuteRecordPO::getRecordId, po.getRecordId()));
    }

    /**
     * {@inheritDoc}
     * @param jobId 作业ID
     * @param contextId 上下文ID
     * @return
     */
    @Override
    public JobContext getContext(String jobId, String contextId) {
        JobExecuteRecordPO po = mapper.selectOne(Wrappers.<JobExecuteRecordPO>lambdaQuery()
                .eq(JobExecuteRecordPO::getJobId, jobId)
                .eq(JobExecuteRecordPO::getRecordId, contextId));
        return converter.reverse().convert(po);
    }

    /**
     * {@inheritDoc}
     * @param jobId 作业ID
     * @return
     */
    @Override
    public JobContext getLatestContext(String jobId) {
        JobExecuteRecordPO po = mapper.selectOne(Wrappers.<JobExecuteRecordPO>lambdaQuery()
                .eq(JobExecuteRecordPO::getJobId, jobId)
                .orderByDesc(JobExecuteRecordPO::getCreatedAt)
                .last("limit 1"));
        return converter.reverse().convert(po);
    }

}
