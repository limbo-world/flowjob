package org.limbo.flowjob.tracker.core.executor;

import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatcher;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobExecutorService implements JobExecutorService {

    /**
     * 作业repository
     */
    private JobRepository jobRepository;

    public SimpleJobExecutorService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * {@inheritDoc}
     * @param tracker tracker节点
     * @param context 待执行的作业上下文
     */
    @Override
    public void execute(JobTracker tracker, JobContext context) {

        Job job = jobRepository.getJob(context.getJobId());
        JobDispatcher dispatcher = job.getDispatchType().newDispatcher(tracker, context);

        dispatcher.dispatch(context, tracker.availableWorkers(), JobContext::startupContext);

    }

}
