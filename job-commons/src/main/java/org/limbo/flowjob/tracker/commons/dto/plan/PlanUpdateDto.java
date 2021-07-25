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
@Schema(title = "修改计划参数")
public class PlanUpdateDto {

    /**
     * 计划描述
     */
    private String planDesc;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOptionDto scheduleOption;

    /**
     * job
     */
    private List<JobDto> jobs;

}
