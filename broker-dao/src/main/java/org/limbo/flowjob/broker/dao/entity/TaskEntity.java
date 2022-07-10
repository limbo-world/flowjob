package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
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

    private Long jobInstanceId;

    /**
     * 状态
     */
    private Byte state;

    /**
     * 成功/失败
     */
    private Byte result;

    /**
     * 执行作业的worker ID
     */
    private Long workerId;

    /**
     * sharding normal
     */
    private Byte type;

    /**
     * 此次执行的参数
     */
    private String attributes;

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

}
