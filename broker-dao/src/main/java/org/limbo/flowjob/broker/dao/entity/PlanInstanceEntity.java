package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
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

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String planInstanceId;

    private String planId;

    private String planInfoId;

    /**
     * 状态
     */
    private Byte status;

    /**
     * 触发类型
     */
    private Byte triggerType;

    /**
     * 期望的调度触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 此次执行的参数
     */
    private String context;

    /**
     * 执行开始时间
     */
    private LocalDateTime startAt;

    /**
     * 执行结束时间
     */
    private LocalDateTime feedbackAt;

    @Override
    public Object getUid() {
        return planInstanceId;
    }
}
