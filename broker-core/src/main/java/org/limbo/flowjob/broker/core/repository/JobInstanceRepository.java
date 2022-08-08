package org.limbo.flowjob.broker.core.repository;

import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;

import java.util.Collection;
import java.util.List;

/**
 * todo
 * @author Devil
 * @since 2021/7/24
 */
public interface JobInstanceRepository {

    /**
     * 新增作业实例，持久化到DB。
     */
    String add(JobInstance jobInstance);


    /**
     * 作业执行成功，CAS 将此作业实例状态从 {@link JobStatus#EXECUTING} 修改为 {@link JobStatus#SUCCEED}
     *
     * @param instance 作业实例
     * @return 更新是否成功
     */
    boolean executeSucceed(JobInstance instance);


    /**
     * 作业执行失败，CAS 将此作业实例状态从 {@link JobStatus#EXECUTING} 修改为 {@link JobStatus#FAILED}
     *
     * @param instance 作业实例
     * @return 更新是否成功
     */
    boolean executeFailed(JobInstance instance);


    /**
     * 根据ID获取指定作业实例
     */
    JobInstance get(String jobInstanceId);


    /**
     * 查询计划实例中，指定作业ID的作业实例。
     * @param planInstanceId 计划实例ID
     * @param jobIds 作业ID集合
     */
    List<JobInstance> listInstances(String planInstanceId, Collection<String> jobIds);

}
