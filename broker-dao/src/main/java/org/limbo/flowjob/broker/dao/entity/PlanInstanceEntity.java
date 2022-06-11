package org.limbo.flowjob.broker.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * plan 一次执行实例 包含整个生命周期
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_plan_instance")
public class PlanInstanceEntity extends Entity {

    private static final long serialVersionUID = -8999288394853231265L;

    /**
     * 全局唯一
     */
    @TableId(type = IdType.INPUT)
    private String planInstanceId;

    /**
     * 对应计划的版本
     */
    private String planInfoId;

    /**
     * 已经重试的次数
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
