package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 最小的执行单元 下发给worker
 *
 * @author Devil
 * @since 2021/9/1
 */
@Setter
@Getter
@Table(name = "flowjob_task")
@Entity
@DynamicInsert
@DynamicUpdate
public class TaskEntity extends BaseEntity {
    private static final long serialVersionUID = -6865597903513656450L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String taskId;

    private String jobInstanceId;

    private String jobId;

    private String planId;

    private String planInstanceId;

    private String planInfoId;

    /**
     * 执行作业的worker ID
     */
    private String workerId;

    /**
     * 类型
     * @see TaskType
     */
    private Byte type;

    /**
     * 状态
     * @see TaskStatus
     */
    private Byte status;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 执行上下文
     */
    private String context;

    /**
     * 此次执行的job参数
     */
    private String jobAttributes;

    /**
     * 此次执行的task参数
     */
    private String taskAttributes;

    /**
     * 作业分发配置参数
     *
     * @see DispatchOption
     */
    private String dispatchOption;

    /**
     * 此次执行返回的参数
     */
    private String result;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    @Override
    public Object getUid() {
        return taskId;
    }
}
