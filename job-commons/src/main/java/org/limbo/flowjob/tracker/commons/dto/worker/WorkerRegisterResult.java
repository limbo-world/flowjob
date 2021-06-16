package org.limbo.flowjob.tracker.commons.dto.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNode;

import java.util.List;

/**
 * worker注册结果
 *
 * @author Brozen
 * @since 2021-06-16
 */
@Data
@Schema(title = "worker注册结果")
public class WorkerRegisterResult {

    @Schema(description = "workerId的字符串形式，由protocol、ip、port决定")
    private String workerId;

    @Schema(description = "tracker节点列表，主从模式下，列表中仅包括一个主节点")
    private List<TrackerNode> trackers;

}
