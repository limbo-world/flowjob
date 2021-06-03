package org.limbo.flowjob.tracker.infrastructure.worker.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.tracker.dao.mybatis.WorkerStatisticsMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerStatisticsPO;
import org.limbo.flowjob.tracker.infrastructure.worker.converters.WorkerStatisticsPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Repository
public class MyBatisWorkerStatisticsRepo implements WorkerStatisticsRepository {

    @Autowired
    private WorkerStatisticsMapper mapper;

    @Autowired
    private WorkerStatisticsPoConverter converter;

    /**
     * {@inheritDoc}
     * @param statistics worker统计记录
     */
    @Override
    public void addOrUpdateWorkerStatistics(WorkerStatistics statistics) {
        WorkerStatisticsPO po = converter.convert(statistics);
        Objects.requireNonNull(po);

        int effected = mapper.update(po, Wrappers.<WorkerStatisticsPO>lambdaUpdate()
                .eq(WorkerStatisticsPO::getWorkerId, po.getWorkerId()));
        if (effected <= 0) {

            effected = mapper.insertIgnore(po);
            if (effected != 1) {
                throw new IllegalStateException(String.format("Update worker error, effected %s rows", effected));
            }
        }
    }

    /**
     * {@inheritDoc}
     * @param workerId workerId
     * @return
     */
    @Override
    public WorkerStatistics getWorkerStatistics(String workerId) {
        return converter.reverse().convert(mapper.selectById(workerId));
    }

    /**
     * {@inheritDoc}
     * @param workerId
     * @param dispatchAt
     * @return
     */
    @Override
    public boolean updateWorkerDispatchTimes(String workerId, LocalDateTime dispatchAt) {
        // TODO
        return false;
    }
}
