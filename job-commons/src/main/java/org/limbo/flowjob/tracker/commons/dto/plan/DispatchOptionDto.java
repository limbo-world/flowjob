package org.limbo.flowjob.tracker.commons.dto.plan;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
public class DispatchOptionDto {

    /**
     * 作业分发方式
     */
    private DispatchType dispatchType;

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     */
    private Float cpuRequirement;

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     */
    private Float ramRequirement;

}
