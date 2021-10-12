package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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

    private String planId;

    private Long planRecordId;

    private Integer planInstanceId;

    private String jobId;

    private Integer jobInstanceId;

    /**
     * 状态
     */
    private Byte state;
    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;
}
