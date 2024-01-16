package org.limbo.flowjob.api.dto.console;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;

import java.io.Serializable;

/**
 * 最小的执行单元 下发给worker
 *
 * @author Devil
 * @since 2021/9/1
 */
@Data
@Schema(title = "任务实例")
public class TaskDTO implements Serializable {

    private static final long serialVersionUID = 3715594289001150342L;

    @Schema(title = "id")
    private String taskId;

    private String jobInstanceId;

    @Schema(title = "执行作业的worker")
    private String workerId;

    private String workerAddress;

    /**
     * 类型
     * @see TaskType
     */
    @Schema(title = "类型")
    private Integer type;

    /**
     * 状态
     * @see TaskStatus
     */
    @Schema(title = "状态")
    private Integer status;

    /**
     * 执行上下文
     */
    @Schema(title = "执行上下文")
    private String context;

    /**
     * 此次执行的job参数
     */
    @Schema(title = "配置参数")
    private String jobAttributes;

    /**
     * 此次执行的task参数
     */
    @Schema(title = "task参数")
    private String taskAttributes;

    /**
     * 此次执行返回的参数
     */
    @Schema(title = "结果")
    private String result;

    /**
     * 执行失败时的异常信息
     */
    @Schema(title = "异常信息")
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    @Schema(title = "异常堆栈")
    private String errorStackTrace;

    /**
     * 开始时间
     */
    @Schema(title = "开始时间")
    private Long startAt;

    /**
     * 结束时间
     */
    @Schema(title = "结束时间")
    private Long endAt;

}
