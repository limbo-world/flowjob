package org.limbo.flowjob.broker.core.repository;

import org.limbo.flowjob.broker.core.plan.job.JobInstance;

import java.util.Collection;
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
     * 根据ID获取指定作业实例
     */
    JobInstance get(String jobInstanceId);

}
