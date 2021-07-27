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
@Schema(title = "新增计划参数")
public class PlanAddDto {

    /**
     * 作业计划ID
     */
    @Schema(title = "作业计划ID")
    private String planId;

    /**
     * 计划描述
     */
    @Schema(title = "计划描述")
    private String planDesc;

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
