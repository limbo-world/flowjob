package org.limbo.flowjob.broker.ha.election.rpc.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import org.limbo.flowjob.broker.ha.election.ElectionTrackerNode;
import org.limbo.flowjob.broker.ha.election.rpc.request.UnscheduleRequest;

/**
 * @author Devil
 * @since 2021/8/11
 */
public class UnscheduleProcessor implements RpcProcessor<UnscheduleRequest> {

    private ElectionTrackerNode trackerNode;

    public UnscheduleProcessor(ElectionTrackerNode trackerNode) {
        this.trackerNode = trackerNode;
    }

    @Override
    public void handleRequest(RpcContext rpcCtx, UnscheduleRequest request) {
        trackerNode.jobTracker().unschedule(request.getId());
        // todo
        rpcCtx.sendResponse(null);
    }

    @Override
    public String interest() {
        return UnscheduleRequest.class.getName();
    }

}
