package org.limbo.flowjob.api.dto.console;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.api.constants.JobStatus;

import java.time.LocalDateTime;

/**
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@Schema(title = "任务实例")
public class JobInstanceDTO {

    @Schema(title = "id")
    private String jobInstanceId;

    private String planInstanceId;

    private String planId;

    private String planInfoId;

    /**
     * DAG中的jobId
     */
    @Schema(title = "DAG中的jobId")
    private String jobId;

    /**
     * 状态
     * @see JobStatus
     */
    @Schema(title = "状态")
    private Integer status;

    /**
     * 当前是第几次重试
     */
    @Schema(title = "重试次数")
    private Integer retryTimes;

    /**
     * 错误信息
     */
    @Schema(title = "错误信息")
    private String errorMsg;

    /**
     * 执行上下文
     */
    @Schema(title = "上下文")
    private String context;

    /**
     * 计划时间
     */
    @Schema(title = "计划时间")
    private LocalDateTime triggerAt;

    /**
     * 开始时间
     */
    @Schema(title = "开始时间")
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    @Schema(title = "结束时间")
    private LocalDateTime endAt;

}
