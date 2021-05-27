package org.limbo.flowjob.tracker.core.job.context;

import org.limbo.flowjob.tracker.core.job.attribute.JobAttributes;

import java.time.LocalDateTime;

/**
 * 作业上下文的属性getter定义
 *
 * @author Brozen
 * @since 2021-05-27
 */
public interface JobContextDefinition {

    /**
     * 获取作业ID。
     * @return 当前执行中的作业ID。
     */
    String getJobId();

    /**
     * 获取当前作业上下文ID。一个作业可能在调度中，有两次同时在执行，因此可能会产生两个context，需要用contextId做区分。
     * @return 当前作业上下文ID
     */
    String getContextId();

    /**
     * 获取当前作业上下文状态。
     * @return 当前上下文状态
     */
    JobContext.Status getStatus();

    /**
     * 获取作业属性。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     * @return {@link JobAttributes}
     */
    JobAttributes getJobAttributes();

    /**
     * 获取执行此作业的worker id
     * @return 执行此作业的worker id
     */
    String getWorkerId();

    /**
     * 获取此上下文的创建时间
     * @return 此上下文的创建时间
     */
    LocalDateTime getCreatedAt();

    /**
     * 获取此上下文的更新时间
     * @return 此上下文的更新时间
     */
    LocalDateTime getUpdatedAt();

}
