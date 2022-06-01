package org.limbo.flowjob.broker.api.param.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业")
public class JobAddParam {

    /**
     * 作业ID
     */
    @Schema(title = "作业ID")
    private String jobId;

    /**
     * 作业描述
     */
    @Schema(title = "作业描述")
    private String description;

    /**
     * 此作业相连的下级作业ID
     */
    @Schema(title = "此作业相连的下级作业ID")
    private Set<String> childrenIds;

    /**
     * 作业分发配置参数
     */
    @Schema(title = "作业分发配置参数")
    private DispatchOptionParam dispatchOption;

    /**
     * 执行器配置参数
     */
    @Schema(title = "执行器配置参数")
    private ExecutorOptionParam executorOption;
}
