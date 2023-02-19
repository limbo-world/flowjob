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
 * job的一次执行 对应于PlanRecord
 *
 * @author Devil
 * @since 2021/9/1
 */
@Setter
@Getter
@Table(name = "flowjob_job_instance")
@Entity
@DynamicInsert
@DynamicUpdate
public class JobInstanceEntity extends BaseEntity {
    private static final long serialVersionUID = -1136312243146520057L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String jobInstanceId;

    private String planInstanceId;

    private String planId;

    private String planInfoId;

    /**
     * DAG中的jobId
     */
    private String jobId;

    /**
     * 状态
     * @see org.limbo.flowjob.common.constants.JobStatus
     */
    private Byte status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行上下文
     */
    private String context;

    /**
     * 调度触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 执行开始时间
     */
    private LocalDateTime startAt;

    /**
     * 执行结束时间
     */
    private LocalDateTime endAt;

    /**
     * 数据版本
     */
    private Integer version;

    @Override
    public Object getUid() {
        return jobInstanceId;
    }
}
