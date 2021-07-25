package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * job 的一次执行记录
 *
 * @author Devil
 * @since 2021/7/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_job_instance")
public class JobInstancePO extends PO {
    private static final long serialVersionUID = 6964053679870383875L;

    /**
     * DB自增序列ID 唯一
     */
    private Long serialId;

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 计划的版本
     */
    private Integer version;

    /**
     * 计划实例的ID
     */
    private Integer planInstanceId;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 从 1 开始增加 planId + version + planInstanceId + jobId + jobInstanceId 全局唯一
     */
    private Integer jobInstanceId;

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
