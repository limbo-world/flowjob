package org.limbo.flowjob.tracker.dao.po;

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
    private String jobId;
}
