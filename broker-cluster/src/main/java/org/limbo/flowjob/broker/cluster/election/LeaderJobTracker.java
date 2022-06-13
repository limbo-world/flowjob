package org.limbo.flowjob.broker.cluster.election;

import org.limbo.flowjob.broker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.broker.cluster.single.SingleJobTracker;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class LeaderJobTracker extends SingleJobTracker {

    public LeaderJobTracker(Scheduler scheduler) {
        super(scheduler);
    }

}
