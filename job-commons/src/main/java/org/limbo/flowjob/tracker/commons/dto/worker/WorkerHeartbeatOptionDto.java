package org.limbo.flowjob.tracker.commons.dto.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * worker 心跳
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Data
@Schema(title = "worker心跳参数")
public class WorkerHeartbeatOptionDto implements Serializable {

    private static final long serialVersionUID = 6512801979734188678L;
    /**
     * worker可用的资源
     */
    @Schema(description = "worker可用的资源")
    private WorkerResourceDto availableResource;

}
