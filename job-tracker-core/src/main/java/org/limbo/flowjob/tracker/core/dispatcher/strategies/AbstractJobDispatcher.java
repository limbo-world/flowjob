package org.limbo.flowjob.tracker.core.dispatcher.strategies;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerDO;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public abstract class AbstractJobDispatcher implements JobDispatcher {

    /**
     * {@inheritDoc}
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @param callback 作业执行回调
     */
    @Override
    public void dispatch(JobContextDO context, Collection<WorkerDO> workers, BiConsumer<JobContextDO, WorkerDO> callback) {
        if (CollectionUtils.isEmpty(workers)) {
            throw new JobWorkerException(context.getJobId(), null, "No worker available!");
        }

        WorkerDO worker = selectWorker(context, workers);
        callback.accept(context, worker);
    }

    /**
     * 选择一个worker进行作业下发
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @return 需要下发作业上下文的worker
     */
    protected abstract WorkerDO selectWorker(JobContextDO context, Collection<WorkerDO> workers);

}
