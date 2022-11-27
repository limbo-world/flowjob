package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
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

    @Id
    private String planInstanceId;

    private String planId;

    private Integer planVersion;

    /**
     * 状态
     */
    private Byte status;

    /**
     * 触发类型
     */
    private Byte triggerType;

    /**
     * 预计触发时间
     */
    private LocalDateTime expectTriggerAt;

    /**
     * 真实触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 执行完成反馈时间
     */
    private LocalDateTime feedbackAt;
}
