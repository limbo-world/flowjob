package org.limbo.flowjob.tracker.core.executor.dispatcher;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.core.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;

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
     * @param executor 作业执行回调
     */
    @Override
    public void dispatch(JobContext context, Collection<Worker> workers, BiConsumer<JobContext, Worker> executor) {
        if (CollectionUtils.isEmpty(workers)) {
            throw new JobWorkerException(context.getJobId(), null, "No worker available!");
        }

        Worker worker = selectWorker(context, workers);
        executor.accept(context, worker);
    }

    /**
     * 选择一个worker进行作业下发
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @return 需要下发作业上下文的worker
     */
    protected abstract Worker selectWorker(JobContext context, Collection<Worker> workers);

}
