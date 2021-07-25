package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 计划实例
 *
 * @author Devil
 * @since 2021/7/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_plan_instance")
public class PlanInstancePO extends PO {

    private static final long serialVersionUID = -8354897444427352804L;
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
     * 从 1 开始增加 planId + version + planInstanceId 全局唯一
     */
    private Integer planInstanceId;

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
