package org.limbo.flowjob.tracker.core.tracker.single;

import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchLauncher;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * 当前节点处理所有的需求
 *
 * @author Devil
 * @since 2021/8/9
 */
public class SingleJobTracker implements JobTracker {

    private final JobInstanceStorage jobInstanceStorage;

    private final Scheduler scheduler;

    private final JobDispatchLauncher jobDispatchLauncher;

    public SingleJobTracker(JobInstanceStorage jobInstanceStorage, Scheduler scheduler,
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

}
