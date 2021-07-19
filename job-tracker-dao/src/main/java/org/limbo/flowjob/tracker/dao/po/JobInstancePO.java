package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author Devil
 * @date 2021/7/15 10:14 上午
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_instance")
public class JobInstancePO extends PO {
    private static final long serialVersionUID = 6964053679870383875L;

    /**
     * DB自增序列ID 唯一
     */
    private Long serialId;

    /**
     * 作业ID
     */
    @TableId(type = IdType.INPUT)
    private String jobInstanceId;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 状态
     */
    private Byte state;

    /**
     * 优先级
     */
    private Integer priority;
}
