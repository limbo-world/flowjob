package org.limbo.flowjob.tracker.commons.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;

import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "计划覆盖参数")
public class PlanReplaceDto {

    /**
     * 计划描述
     */
    @Schema(title = "计划描述")
    private String description;

    /**
     * 作业计划调度配置参数
     */
    @Schema(title = "作业计划调度配置参数")
    private ScheduleOptionDto scheduleOption;

    /**
     * 此执行计划对应的所有作业
     */
    @Schema(title = "此执行计划对应的所有作业")
    private List<JobDto> jobs;

}
