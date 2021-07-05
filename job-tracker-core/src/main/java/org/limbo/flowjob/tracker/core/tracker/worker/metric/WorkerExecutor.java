package org.limbo.flowjob.tracker.core.tracker.worker.metric;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobExecuteType;

/**
 * worker可用的执行器
 *
 * @author Brozen
 * @since 2021-07-01
 */
@Data
public class WorkerExecutor {

    /**
     * worker节点ID
     */
    private String workerId;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 执行器描述信息
     */
    private String executorDesc;

    /**
     * 执行器类型
     */
    private JobExecuteType executeType;

}
