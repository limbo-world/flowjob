package org.limbo.flowjob.tracker.core.tracker.election.rpc.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import org.limbo.flowjob.tracker.core.tracker.election.ElectionTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.request.ScheduleRequest;

/**
 * @author Devil
 * @since 2021/8/11
 */
public class ScheduleProcessor implements RpcProcessor<ScheduleRequest> {

    private ElectionTrackerNode trackerNode;

    public ScheduleProcessor(ElectionTrackerNode trackerNode) {
        this.trackerNode = trackerNode;
    }

    @Override
    public void handleRequest(RpcContext rpcCtx, ScheduleRequest request) {
        trackerNode.jobTracker().schedule(request.getSchedulable());
        // todo
        rpcCtx.sendResponse(null);
    }

    @Override
    public String interest() {
        return ScheduleRequest.class.getName();
    }

}
