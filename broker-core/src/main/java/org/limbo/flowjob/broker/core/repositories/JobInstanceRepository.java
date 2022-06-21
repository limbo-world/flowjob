package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;

import java.util.Collection;
import java.util.List;

/**
 * todo
 * @author Devil
 * @since 2021/7/24
 */
public interface JobInstanceRepository {

    /**
     * 持久化
     */
    String add(JobInstance jobInstance);

    /**
     * 获取
     */
    JobInstance get(String jobInstanceId);


    /**
     * CAS 将此作业执行记录状态从 {@link JobScheduleStatus#SCHEDULING} 修改为 {@link JobScheduleStatus#EXECUTING}
     * @param jobInstanceId 待更新的作业执行记录ID
     * @return 更新是否成功
     */
    boolean execute(String jobInstanceId);


    boolean end(String jobInstanceId, JobScheduleStatus state);


    List<JobInstance> getInstances(String planInstanceId, Collection<String> jobIds);

}
