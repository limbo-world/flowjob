package org.limbo.flowjob.tracker.commons.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobExecuteType;

/**
 * @author Devil
 * @since 2021/7/28
 */
@Data
@Schema(title = "执行器配置参数")
public class ExecutorOptionDto {

    /**
     * 执行器名称
     */
    @Schema(title = "执行器名称", description = "执行器名称")
    private String name;

    /**
     * 执行器类型
     */
    @Schema(title = "执行器类型", description = "执行器类型")
    private JobExecuteType type;

}
