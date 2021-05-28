package org.limbo.flowjob.tracker.core.executor;

import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatcher;
import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobExecutorService implements JobExecutorService {

    /**
     * 用于生成JobDispatcher
     */
    private JobDispatcherFactory jobDispatcherFactory;

    public SimpleJobExecutorService(JobDispatcherFactory jobDispatcherFactory) {
        this.jobDispatcherFactory = jobDispatcherFactory;
    }

    /**
     * {@inheritDoc}
     * @param tracker tracker节点
     * @param context 待执行的作业上下文
     */
    @Override
    public void execute(JobTracker tracker, JobContextDO context) {

        JobDispatcher jobDispatcher = jobDispatcherFactory.newDispatcher(tracker, context);
        jobDispatcher.dispatch(context, tracker.availableWorkers(), JobContextDO::startupContext);

    }

}
