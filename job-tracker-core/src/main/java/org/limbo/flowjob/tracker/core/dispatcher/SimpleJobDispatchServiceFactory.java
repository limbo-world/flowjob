package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.core.dispatcher.strategies.DefaultJobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.core.job.context.JobContext;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobDispatchServiceFactory implements JobDispatchServiceFactory {

    private JobRepository jobRepository;

    public SimpleJobDispatchServiceFactory(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * {@inheritDoc}
     * @param context 作业上下文
     * @return
     */
    @Override
    public JobDispatchService newDispatchService(JobContext context) {
        return new SimpleJobDispatchService(new DefaultJobDispatcherFactory(jobRepository));
    }

}
