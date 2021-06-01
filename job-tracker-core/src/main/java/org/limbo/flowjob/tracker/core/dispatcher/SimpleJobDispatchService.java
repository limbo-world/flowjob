package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobDispatchService implements JobDispatchService {

    /**
     * 用于生成JobDispatcher
     */
    private JobDispatcherFactory jobDispatcherFactory;

    public SimpleJobDispatchService(JobDispatcherFactory jobDispatcherFactory) {
        this.jobDispatcherFactory = jobDispatcherFactory;
    }

    /**
     * {@inheritDoc}
     * @param tracker tracker节点
     * @param context 待执行的作业上下文
     */
    @Override
    public void dispatch(JobTracker tracker, JobContextDO context) {

        JobDispatcher jobDispatcher = jobDispatcherFactory.newDispatcher(tracker, context);
        jobDispatcher.dispatch(context, tracker.availableWorkers(), JobContextDO::startupContext);

    }

}
