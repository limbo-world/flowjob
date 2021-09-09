package org.limbo.flowjob.tracker.core.job.context;

import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;

import java.util.List;

/**
 * todo
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
    JobRecord get(String planId, Long planRecordId, Long planInstanceId, String jobId);

    void executing(String planId, Long planRecordId, Long planInstanceId, String jobId);

    void end(String planId, Long planRecordId, Long planInstanceId, String jobId, JobScheduleStatus state);

    List<JobRecord> getRecords(String planId, Long planRecordId, Long planInstanceId, List<String> jobIds);

}
