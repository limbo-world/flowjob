package org.limbo.flowjob.tracker.commons.dto.job;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.dto.plan.DispatchOptionDto;

import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
public class JobDto {

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 作业描述
     */
    private String jobDesc;

    /**
     * 此作业依赖的父作业ID
     */
    private List<String> parentJobIds;

    /**
     * 作业分发配置参数
     */
    private DispatchOptionDto dispatchOption;
}
