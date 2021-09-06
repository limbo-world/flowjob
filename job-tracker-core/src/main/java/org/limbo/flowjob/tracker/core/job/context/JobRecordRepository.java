package org.limbo.flowjob.tracker.core.job.context;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface JobRecordRepository {

    /**
     * 持久化
     */
    void add(JobRecord record);

    /**
     * 获取
     */
    JobRecord get(String planId, Long planRecordId);

    void end();

}
