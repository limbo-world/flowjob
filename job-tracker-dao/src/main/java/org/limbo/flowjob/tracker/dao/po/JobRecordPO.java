package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * job的一次执行 对应于PlanRecord
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_job_record")
public class JobRecordPO extends PO {
    private static final long serialVersionUID = -1136312243146520057L;

    /**
     * DB自增序列ID，并不是唯一标识
     */
    private Long serialId;

    private String planId;

    private Long planRecordId;

    private Long planInstanceId;

    private String jobId;

    /**
     * 状态
     */
    private Byte state;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 已经重试的次数 todo 可以不要这个字段，直接从db获取instance个数   不管用不用这个字段，可能存在worker重复反馈导致数据问题
     */
    private Integer retry;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;
}
