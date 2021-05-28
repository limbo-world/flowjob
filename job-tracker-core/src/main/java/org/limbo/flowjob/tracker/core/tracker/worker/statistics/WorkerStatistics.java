package org.limbo.flowjob.tracker.core.tracker.worker.statistics;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * worker执行任务统计信息
 *
 * @author Brozen
 * @since 2021-05-28
 */
public class WorkerStatistics {

    /**
     * 对应worker的ID
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String workerId;

    /**
     * 作业下发到此worker的次数
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Long jobDispatchCount;

    /**
     * 最后一次向此worker下发任务成功的时间
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private LocalDateTime latestDispatchTime;

}
