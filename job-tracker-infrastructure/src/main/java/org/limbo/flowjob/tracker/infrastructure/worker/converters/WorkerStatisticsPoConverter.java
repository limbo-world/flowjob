package org.limbo.flowjob.tracker.infrastructure.worker.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.tracker.dao.po.WorkerStatisticsPO;
import org.limbo.utils.EnhancedBeanUtils;

/**
 * @author Brozen
 * @since 2021-06-03
 */
public class WorkerStatisticsPoConverter extends Converter<WorkerStatistics, WorkerStatisticsPO> {

    /**
     * 将{@link WorkerStatistics}值对象转换为{@link WorkerStatisticsPO}持久化对象
     * @param vo {@link WorkerStatistics}值对象
     * @return {@link WorkerStatisticsPO}持久化对象
     */
    @Override
    protected WorkerStatisticsPO doForward(WorkerStatistics vo) {
        return EnhancedBeanUtils.createAndCopy(vo, WorkerStatisticsPO.class);
    }

    /**
     * 将{@link WorkerStatisticsPO}持久化对象转换为{@link WorkerStatistics}值对象
     * @param po {@link WorkerStatisticsPO}持久化对象
     * @return {@link WorkerStatistics}值对象
     */
    @Override
    protected WorkerStatistics doBackward(WorkerStatisticsPO po) {
        return EnhancedBeanUtils.createAndCopy(po, WorkerStatistics.class);
    }
}
