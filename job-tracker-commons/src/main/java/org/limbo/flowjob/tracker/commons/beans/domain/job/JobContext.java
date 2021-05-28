package org.limbo.flowjob.tracker.commons.beans.domain.job;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-28
 */
@Data
public class JobContext {

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 执行上下文ID。一个作业可能在调度中，有两次同时在执行，因此可能会产生两个context，需要用contextId做区分。
     */
    private String contextId;

    /**
     * 此上下文状态
     */
    private JobContext.Status status;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * 此上下文的创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 此上下文的更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 作业属性，不可变。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     */
    private JobAttributes jobAttributes;


    /**
     * 上下文状态
     */
    public interface Status {

        /**
         * 状态码
         */
        int getStatus();

        /**
         * 状态描述
         */
        String getDesc();
    }

}
