package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * plan 一次执行实例 包含整个生命周期
 *
 * @author Devil
 * @since 2021/9/1
 */
@Setter
@Getter
@Table(name = "flowjob_plan_instance")
@Entity
@DynamicInsert
@DynamicUpdate
public class PlanInstanceEntity extends BaseEntity {

    private static final long serialVersionUID = -8999288394853231265L;

    /**
     * 对应计划的版本
     */
    private Long planInfoId;

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
