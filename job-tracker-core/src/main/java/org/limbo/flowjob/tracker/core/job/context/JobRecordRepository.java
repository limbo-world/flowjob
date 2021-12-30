package org.limbo.flowjob.tracker.core.job.context;

import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;

import java.util.Collection;
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
    JobRecord get(JobRecord.ID jobRecordId);


    /**
     * CAS 将此作业执行记录状态从 {@link JobScheduleStatus#SCHEDULING} 修改为 {@link JobScheduleStatus#EXECUTING}
     * @param jobRecordId 待更新的作业执行记录ID
     * @return 更新是否成功
     */
    boolean execute(JobRecord.ID jobRecordId);


    void end(JobRecord.ID jobRecordId, JobScheduleStatus state);


    List<JobRecord> getRecords(PlanInstance.ID planInstanceId, Collection<String> jobIds);

}
