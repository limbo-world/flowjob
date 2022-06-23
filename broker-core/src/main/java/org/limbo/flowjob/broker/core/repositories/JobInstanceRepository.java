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
     * 作业实例下发成功，CAS 将此作业实例状态从 {@link JobScheduleStatus#SCHEDULING} 修改为 {@link JobScheduleStatus#EXECUTING}
     *
     * @param instance 作业实例
     * @return 更新是否成功
     */
    boolean dispatched(JobInstance instance);

    /**
     * 作业实例下发失败，CAS 将此作业实例状态从 {@link JobScheduleStatus#SCHEDULING} 修改为 {@link JobScheduleStatus#FAILED}
     *
     * @param instance 作业实例
     * @return 更新是否成功
     */
    boolean dispatchFailed(JobInstance instance);


    boolean end(String jobInstanceId, JobScheduleStatus state);


    List<JobInstance> getInstances(String planInstanceId, Collection<String> jobIds);

}
