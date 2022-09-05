package org.limbo.flowjob.broker.core.repository;

import org.limbo.flowjob.broker.core.domain.job.JobInstance;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface JobInstanceRepository {

    /**
     * 新增作业实例，持久化到DB。
     */
    String save(JobInstance jobInstance);

    /**
     * 新增作业实例，持久化到DB。
     */
    void saveAll(List<JobInstance> jobInstances);


    /**
     * 根据ID获取指定作业实例
     */
    JobInstance get(String jobInstanceId);

}
