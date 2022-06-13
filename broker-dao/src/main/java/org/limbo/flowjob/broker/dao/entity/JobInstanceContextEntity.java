package org.limbo.flowjob.broker.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * job 的一次执行上下文
 *
 * @author Devil
 * @since 2021/7/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_job_instance_context")
public class JobInstanceContextEntity extends Entity {
    private static final long serialVersionUID = 6964053679870383875L;

    /**
     * 全局唯一
     */
    @TableId(type = IdType.INPUT)
    private String jobInstanceContextId;

    private String jobInstanceId;

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
