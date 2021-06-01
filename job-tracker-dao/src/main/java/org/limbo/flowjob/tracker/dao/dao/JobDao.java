package org.limbo.flowjob.tracker.dao.dao;

import org.limbo.flowjob.tracker.dao.po.JobPO;

/**
 * @author Brozen
 * @since 2021-06-01
 */
public interface JobDao {

    /**
     * 持久化保存Job到DB，如果jobId不存在则新增，存在则更新
     * @param job 作业Job
     */
    void saveOrUpdateJob(JobPO job);

    /**
     * 根据Job的唯一标识ID查询
     * @return 作业Job
     */
    JobPO getById(String jobId);

}
