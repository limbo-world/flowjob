package org.limbo.flowjob.tracker.core.tracker.worker.statistics;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-28
 */
public interface WorkerStatisticsRepository {

    /**
     * 新增一个worker统计记录，应该在worker新增的时候才新增
     * @param statistics worker统计记录
     */
    void createWorkerStatistics(WorkerStatistics statistics);

    /**
     * 根据workerId查询统计记录
     * @param workerId workerId
     * @return 入参workerId对应的统计记录
     */
    WorkerStatistics getWorkerStatistics(String workerId);

    /**
     * 更新worker的最近作业下发时间
     * @return 是否更新成功
     */
    boolean updateWorkerDispatchTimes(String workerId, LocalDateTime dispatchAt);

}
