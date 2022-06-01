package org.limbo.flowjob.broker.core.plan.job;

import lombok.Data;

/**
 * todo 这个我认为应该是 worker 执行结束后回调 tracker 的时候保存到db
 *
 * @author Devil
 * @since 2021/7/24
 */
@Data
public class JobExecuteRecord {

    private Long id;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 对应实例
     */
    private String jobInstanceId;

    /**
     * 执行状态
     */
    private Byte status;

    /**
     * 执行作业的worker ID
     */
    private String workerId;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;


}
