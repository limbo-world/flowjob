package org.limbo.flowjob.tracker.infrastructure.job.repositories;

import org.limbo.flowjob.tracker.core.job.JobDO;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.dao.dao.JobDao;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.limbo.flowjob.tracker.infrastructure.job.converters.JobPoDoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Job的repo，领域层使用。
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Repository
public class BaseJobRepository implements JobRepository {

    /**
     * DAO层抽象接口
     */
    @Autowired
    private JobDao jobDao;

    /**
     * DO 与 PO 的转换器
     */
    @Autowired
    private JobPoDoConverter converter;

    /**
     * {@inheritDoc}
     * @param job 作业数据
     */
    @Override
    public void addJob(JobDO job) {
        JobPO po = converter.convert(job);
        jobDao.saveOrUpdateJob(po);
    }

    /**
     * {@inheritDoc}
     * @param jobId jobId
     * @return
     */
    @Override
    public JobDO getJob(String jobId) {
        return converter.reverse().convert(jobDao.getById(jobId));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<JobDO> listSchedulableJobs() {
        // TODO
        throw new UnsupportedOperationException("TODO 暂未实现");
    }
}
