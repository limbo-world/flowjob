package org.limbo.flowjob.tracker.commons.dto.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecutorRegisterDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * worker注册时的参数
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Data
@Schema(title = "worker注册参数")
public class WorkerRegisterOptionDto implements Serializable {

    private static final long serialVersionUID = 4234037520144789567L;

    /**
     * worker id
     */
    @NotBlank(message = "worker can't be blank")
    @Schema(description = "worker id")
    private String id;

    /**
     * worker服务使用的通信协议，默认为Http协议。
     */
    @Schema(description = "worker服务使用的通信协议，默认为Http协议", implementation = Integer.class)
    private WorkerProtocol protocol;

    /**
     * worker服务的通信IP
     */
    @NotBlank
    @Schema(description = "worker服务的通信IP")
    private String ip;

    /**
     * worker服务的通信端口
     */
    @NotNull
    @Schema(description = "worker服务的通信端口")
    private Integer port;

    /**
     * worker可用的资源
     */
    @Schema(description = "worker可用的资源")
    private WorkerResourceDto availableResource;

    /**
     * 执行器
     */
    @Schema(description = "job 执行器")
    private List<JobExecutorRegisterDto> jobExecutors;

    /**
     * worker所属租户信息
     */
    @Schema(description = "worker所属租户信息")
    private WorkerTenantDto tenant;

}
