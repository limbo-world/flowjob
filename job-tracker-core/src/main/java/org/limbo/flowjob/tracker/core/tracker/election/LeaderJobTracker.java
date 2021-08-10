package org.limbo.flowjob.tracker.core.tracker.election;

import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNodeDto;
import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchLauncher;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

import java.util.List;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class LeaderJobTracker implements JobTracker {

    private final JobInstanceStorage jobInstanceStorage;

    private final Scheduler scheduler;

    private final JobDispatchLauncher jobDispatchLauncher;

    public LeaderJobTracker(JobInstanceStorage jobInstanceStorage, Scheduler scheduler,
                            JobDispatchLauncher jobDispatchLauncher) {
        this.jobInstanceStorage = jobInstanceStorage;
        this.scheduler = scheduler;
        this.jobDispatchLauncher = jobDispatchLauncher;
    }

    @Override
    public void schedule(Schedulable schedulable) {
        scheduler.schedule(schedulable);
    }

    @Override
    public void unschedule(String id) {
        scheduler.unschedule(id);
    }

    @Override
    public boolean isScheduling(String id) {
        return scheduler.isScheduling(id);
    }

    @Override
    public List<TrackerNodeDto> getTrackerNodes() {
        return null;
    }
}
