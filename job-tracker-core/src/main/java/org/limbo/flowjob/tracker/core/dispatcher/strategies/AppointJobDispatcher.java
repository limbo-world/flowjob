package org.limbo.flowjob.tracker.core.dispatcher.strategies;

import org.limbo.flowjob.tracker.commons.constants.enums.JobDispatchType;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerDO;

import java.util.Collection;

/**
 * @author Brozen
 * @since 2021-05-27
 * @see JobDispatchType#APPOINT
 */
public class AppointJobDispatcher extends AbstractJobDispatcher implements JobDispatcher {

    /**
     * {@inheritDoc}
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @return
     */
    @Override
    protected WorkerDO selectWorker(JobContextDO context, Collection<WorkerDO> workers) {
        // TODO
        return null;
    }
}
