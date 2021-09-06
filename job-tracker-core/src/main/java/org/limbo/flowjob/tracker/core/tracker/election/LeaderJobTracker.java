package org.limbo.flowjob.tracker.core.tracker.election;

import org.limbo.flowjob.tracker.core.dispatcher.DispatchLauncher;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.Storage;
import org.limbo.flowjob.tracker.core.tracker.single.SingleJobTracker;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class LeaderJobTracker extends SingleJobTracker {

    public LeaderJobTracker(Storage storage, Scheduler scheduler, DispatchLauncher dispatchLauncher) {
        super(storage, scheduler, dispatchLauncher);
    }

}
