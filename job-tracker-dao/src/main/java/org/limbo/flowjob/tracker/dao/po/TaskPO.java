package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 最小的执行单元 下发给worker
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_task")
public class TaskPO extends PO {
    private static final long serialVersionUID = -6865597903513656450L;

    /**
     * DB自增序列ID，并不是唯一标识
     */
    private Long serialId;

    private String planId;

    private Long planRecordId;

    private Long planInstanceId;

    private String jobId;

    private Long jobInstanceId;
    /**
     * JobInstance下唯一
     */
    private String taskId;
    /**
     * 状态
     */
    private Byte state;

    /**
     * 执行作业的worker ID
     */
    private String workerId;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;

}
