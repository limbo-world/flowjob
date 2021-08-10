package org.limbo.flowjob.tracker.core.tracker.election;

import com.alipay.sofa.jraft.util.Endpoint;
import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchLauncher;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.RpcCaller;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class ElectionJobTrackerFactory {

    private final JobInstanceStorage jobInstanceStorage;

    private final Scheduler scheduler;

    private final JobDispatchLauncher jobDispatchLauncher;

    public ElectionJobTrackerFactory(JobInstanceStorage jobInstanceStorage, Scheduler scheduler,
                                     JobDispatchLauncher jobDispatchLauncher) {
        this.jobInstanceStorage = jobInstanceStorage;
        this.scheduler = scheduler;
        this.jobDispatchLauncher = jobDispatchLauncher;
    }

    public JobTracker leader() {
        return new LeaderJobTracker(jobInstanceStorage, scheduler, jobDispatchLauncher);
    }

    public JobTracker follower(Endpoint endpoint, RpcCaller rpcCaller) {
        return new FollowerJobTracker(endpoint, rpcCaller);
    }
}
