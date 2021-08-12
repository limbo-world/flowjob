package org.limbo.flowjob.tracker.core.tracker.election;

import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchLauncher;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.single.SingleJobTracker;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class LeaderJobTracker extends SingleJobTracker {

    public LeaderJobTracker(JobInstanceStorage jobInstanceStorage, Scheduler scheduler, JobDispatchLauncher jobDispatchLauncher) {
        super(jobInstanceStorage, scheduler, jobDispatchLauncher);
    }

}
