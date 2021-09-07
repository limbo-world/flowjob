package org.limbo.flowjob.tracker.core.tracker;

import com.alipay.sofa.jraft.util.Endpoint;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.tracker.election.FollowerJobTracker;
import org.limbo.flowjob.tracker.core.tracker.election.LeaderJobTracker;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.RpcCaller;
import org.limbo.flowjob.tracker.core.tracker.single.SingleJobTracker;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class JobTrackerFactory {

    private final Scheduler scheduler;

    public JobTrackerFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public JobTracker leader() {
        return new LeaderJobTracker(scheduler);
    }

    public JobTracker follower(Endpoint endpoint, RpcCaller rpcCaller) {
        return new FollowerJobTracker(endpoint, rpcCaller);
    }

    public JobTracker single() {
        return new SingleJobTracker(scheduler);
    }
}
