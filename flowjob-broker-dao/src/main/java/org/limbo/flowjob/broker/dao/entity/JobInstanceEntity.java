package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.api.constants.JobStatus;

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

    /**
     * 分配的节点 ip:host
     */
    private String brokerUrl;

    private String planInstanceId;

    private String planId;

    private String planInfoId;

    /**
     * DAG中的jobId
     */
    private String jobId;

    private String agentId;

    /**
     * 状态
     * @see JobStatus
     */
    private Integer status;

    /**
     * 当前是第几次重试
     */
    private Integer retryTimes;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行上下文
     */
    private String context;

    /**
     * 计划时间
     */
    private LocalDateTime triggerAt;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    /**
     * 上次上报时间戳，毫秒
     */
    private LocalDateTime lastReportAt;

    @Override
    public Object getUid() {
        return jobInstanceId;
    }
}
