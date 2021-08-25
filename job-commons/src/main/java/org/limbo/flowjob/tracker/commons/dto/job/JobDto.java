package org.limbo.flowjob.tracker.commons.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业")
public class JobDto {

    /**
     * 作业ID
     */
    @Schema(title = "作业ID")
    private String jobId;

    /**
     * 作业描述
     */
    @Schema(title = "作业描述")
    private String jobDesc;

    @Schema(title = "节点类型")
    private String nodeType;

    /**
     * 此作业相连的下级作业ID
     */
    @Schema(title = "此作业相连的下级作业ID")
    private List<String> childrenIds;

    /**
     * 作业分发配置参数
     */
    @Schema(title = "作业分发配置参数")
    private DispatchOptionDto dispatchOption;

    /**
     * 执行器配置参数
     */
    @Schema(title = "执行器配置参数")
    private ExecutorOptionDto executorOption;
}
