package org.limbo.flowjob.tracker.commons.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.dto.job.JobAddDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobUpdateDto;

import java.util.List;

/**
 * @author Devil
 * @date 2021/7/14 2:01 下午
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
     * 需要新增的 job
     */
    private List<JobAddDto> addJobs;

    /**
     * 需要更新的 job
     */
    private List<JobUpdateDto> updateJobs;

    /**
     * 需要删除的 job
     */
    private List<String> deleteJobIds;

}
