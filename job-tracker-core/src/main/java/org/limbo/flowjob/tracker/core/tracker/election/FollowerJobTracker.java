package org.limbo.flowjob.tracker.core.tracker.election;

import com.alipay.sofa.jraft.util.Endpoint;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.RpcCaller;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.ScheduleRequest;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.ScheduleRequestType;

/**
 * 远程调用 leader
 *
 * @author Devil
 * @since 2021/8/9
 */
public class FollowerJobTracker implements JobTracker {

    private Endpoint endpoint;

    private RpcCaller rpcCaller;

    public FollowerJobTracker(Endpoint endpoint, RpcCaller rpcCaller) {
        this.endpoint = endpoint;
        this.rpcCaller = rpcCaller;
    }

    @Override
    public void schedule(Schedulable schedulable) {
        ScheduleRequest request = new ScheduleRequest();
        request.setType(ScheduleRequestType.SCHEDULE);
        request.setSchedulable(schedulable);

        rpcCaller.invokeSync(endpoint, request);
    }

    @Override
    public void unschedule(String id) {
        ScheduleRequest request = new ScheduleRequest();
        request.setType(ScheduleRequestType.UNSCHEDULE);
        request.setId(id);

        rpcCaller.invokeSync(endpoint, request);
    }

    @Override
    public boolean isScheduling(String id) {
        ScheduleRequest request = new ScheduleRequest();
        request.setType(ScheduleRequestType.IS_SCHEDULING);
        request.setId(id);

        return (boolean) rpcCaller.invokeSync(endpoint, request);
    }
}
