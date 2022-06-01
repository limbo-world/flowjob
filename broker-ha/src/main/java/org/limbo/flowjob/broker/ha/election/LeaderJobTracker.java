package org.limbo.flowjob.broker.ha.election;

import org.limbo.flowjob.broker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.broker.ha.single.SingleJobTracker;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class LeaderJobTracker extends SingleJobTracker {

    public LeaderJobTracker(Scheduler scheduler) {
        super(scheduler);
    }

}
