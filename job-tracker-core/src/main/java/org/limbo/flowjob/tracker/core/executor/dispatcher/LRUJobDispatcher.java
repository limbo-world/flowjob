package org.limbo.flowjob.tracker.core.executor.dispatcher;

import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;

import java.util.Collection;

/**
 * @author Brozen
 * @since 2021-05-27
 * @see JobDispatchType#LEAST_RECENTLY_USED
 */
public class LRUJobDispatcher extends AbstractJobDispatcher implements JobDispatcher {

    /**
     * {@inheritDoc}
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @return
     */
    @Override
    protected Worker selectWorker(JobContext context, Collection<Worker> workers) {
        // TODO
        return null;
    }

}
