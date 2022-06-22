package org.limbo.flowjob.broker.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 最小的执行单元 下发给worker
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_task")
public class TaskEntity extends BaseEntityMeta {
    private static final long serialVersionUID = -6865597903513656450L;

    /**
     * JobInstance下唯一
     */
    private String taskId;

    private String planId;

    private String planInstanceId;

    private String jobId;

    private String jobInstanceId;

    /**
     * 状态
     */
    private Byte state;

    /**
     * 成功/失败
     */
    private Byte result;

    /**
     * 执行作业的worker ID
     */
    private String workerId;

    /**
     * sharding normal
     */
    private Byte type;

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

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

}
