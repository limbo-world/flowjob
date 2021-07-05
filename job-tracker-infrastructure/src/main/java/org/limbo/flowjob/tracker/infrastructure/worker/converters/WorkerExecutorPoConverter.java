package org.limbo.flowjob.tracker.infrastructure.worker.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerExecutor;
import org.limbo.flowjob.tracker.dao.po.WorkerExecutorPO;
import org.limbo.utils.EnhancedBeanUtils;

import javax.annotation.Nonnull;

/**
 * @author Brozen
 * @since 2021-07-05
 */
public class WorkerExecutorPoConverter extends Converter<WorkerExecutor, WorkerExecutorPO> {

    /**
     * 将 {@link WorkerExecutor} 转换为 {@link WorkerExecutorPO}
     * @param vo {@link WorkerExecutor} 值对象
     * @return {@link WorkerExecutorPO} 持久化对象
     */
    @Override
    protected WorkerExecutorPO doForward(@Nonnull WorkerExecutor vo) {
        return EnhancedBeanUtils.createAndCopy(vo, WorkerExecutorPO.class);
    }


    /**
     * 将 {@link WorkerExecutorPO} 转换为 {@link WorkerExecutor}
     * @param po {@link WorkerExecutorPO} 持久化对象
     * @return {@link WorkerExecutor} 值对象
     */
    @Override
    protected WorkerExecutor doBackward(@Nonnull WorkerExecutorPO po) {
        return EnhancedBeanUtils.createAndCopy(po, WorkerExecutor.class);
    }
}
