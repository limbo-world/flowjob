package org.limbo.flowjob.tracker.core.dispatcher.strategies;

import org.limbo.flowjob.tracker.commons.constants.enums.JobDispatchType;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;

import java.util.Collection;
import java.util.Random;

/**
 * @author Brozen
 * @since 2021-05-27
 * @see JobDispatchType#RANDOM
 */
public class RandomJobDispatcher extends AbstractJobDispatcher implements JobDispatcher {

    private Random random;

    public RandomJobDispatcher() {
        this.random = new Random();
    }

    /**
     * {@inheritDoc}
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @return
     */
    @Override
    protected Worker selectWorker(JobContext context, Collection<Worker> workers) {
        int index = this.random.nextInt(workers.size() + 1) - 1;

        int i = 0;
        for (Worker worker : workers) {
            if (i == index) {
                return worker;
            }
        }

        throw new JobWorkerException(context.getJobId(), null, "No worker available!");
    }

}
