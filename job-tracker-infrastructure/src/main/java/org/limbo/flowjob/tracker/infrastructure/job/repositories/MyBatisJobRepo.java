package org.limbo.flowjob.tracker.infrastructure.job.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.dao.mybatis.JobMapper;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

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
    public void addJob(Job job) {
        JobPO po = converter.convert(job);
        Objects.requireNonNull(po);

        int effected = mapper.insertIgnore(po);
        if (effected <= 0) {
            effected = mapper.update(po, Wrappers.<JobPO>lambdaUpdate()
                    .eq(JobPO::getJobId, po.getJobId()));

            if (effected != 1) {
                throw new IllegalStateException(String.format("Update job error, effected %s rows", effected));
            }
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

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<Job> listSchedulableJobs() {
        // TODO
        throw new UnsupportedOperationException("TODO 暂未实现");
    }
}
