package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_job_link")
public class JobLinkPO extends PO {

    private static final long serialVersionUID = 6309009277259716977L;

    /**
     * DB自增序列ID 唯一
     */
    private Long serialId;

    private String parentJobId;

    private String jobId;

    private Integer version;
}
