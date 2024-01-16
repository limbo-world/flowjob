package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.api.constants.InstanceStatus;
import org.limbo.flowjob.api.constants.InstanceType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Setter
@Getter
@Table(name = "flowjob_delay_instance")
@Entity
@DynamicInsert
@DynamicUpdate
public class DelayInstanceEntity extends BaseEntity {

    private static final long serialVersionUID = 6194181500527493339L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String instanceId;

    /**
     * 主题
     */
    private String bizType;

    /**
     * 业务ID type + id 唯一
     */
    private String bizId;

    /**
     * 实例类型
     * @see InstanceType
     */
    private Integer instanceType;

    /**
     * 状态
     *
     * @see InstanceStatus
     */
    private Integer status;

    /**
     * single 存储job信息
     * workflow存储job节点之间的关联关系
     */
    private String jobInfo;

    /**
     * 属性参数
     */
    protected String attributes;

    /**
     * 期望的调度触发时间
     */
    private LocalDateTime triggerAt;

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
        return instanceId;
    }
}
