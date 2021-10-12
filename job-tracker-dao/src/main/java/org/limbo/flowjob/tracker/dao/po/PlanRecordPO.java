package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * plan的一次执行
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_plan_record")
public class PlanRecordPO extends PO {

    private static final long serialVersionUID = -8999288394853231265L;
    /**
     * DB自增序列ID，并不是唯一标识
     */
    private Long serialId;

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 从 1 开始增加 planId + recordId 全局唯一
     */
    private Long planRecordId;

    /**
     * 计划的版本
     */
    private Integer version;

    /**
     * 重试次数
     */
    private Integer retry;

    /**
     * 状态
     */
    private Byte state;

    /**
     * 是否手动下发
     */
    private Boolean manual;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;
}
