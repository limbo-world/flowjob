package org.limbo.flowjob.tracker.core.executor;

import org.limbo.flowjob.tracker.core.executor.dispatcher.DefaultJobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobExecutorServiceFactory implements JobExecutorServiceFactory {

    private JobRepository jobRepository;

    public SimpleJobExecutorServiceFactory(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * {@inheritDoc}
     * @param context 作业上下文
     * @return
     */
    @Override
    public JobExecutorService newExecutorService(JobContextDO context) {
        return new SimpleJobExecutorService(new DefaultJobDispatcherFactory(jobRepository));
    }

}
