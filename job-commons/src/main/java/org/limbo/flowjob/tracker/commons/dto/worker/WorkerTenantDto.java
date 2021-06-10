package org.limbo.flowjob.tracker.commons.dto.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * worker所属租户信息参数
 * TODO 待实现
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Data
@Schema(title = "worker的租户信息参数")
public class WorkerTenantDto implements Serializable {

    private static final long serialVersionUID = 3341023110953492576L;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private String tenantId;

}
