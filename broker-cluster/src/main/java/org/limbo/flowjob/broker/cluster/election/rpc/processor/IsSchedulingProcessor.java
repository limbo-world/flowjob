//package org.limbo.flowjob.broker.cluster.election.rpc.processor;
//
//import com.alipay.sofa.jraft.rpc.RpcContext;
//import com.alipay.sofa.jraft.rpc.RpcProcessor;
//import org.limbo.flowjob.broker.cluster.election.ElectionTrackerNode;
//import org.limbo.flowjob.broker.cluster.election.rpc.request.IsSchedulingRequest;
//
///**
// * @author Devil
// * @since 2021/8/11
// */
//public class IsSchedulingProcessor implements RpcProcessor<IsSchedulingRequest> {
//
//    private ElectionTrackerNode trackerNode;
//
//    public IsSchedulingProcessor(ElectionTrackerNode trackerNode) {
//        this.trackerNode = trackerNode;
//    }
//
//    @Override
//    public void handleRequest(RpcContext rpcCtx, IsSchedulingRequest request) {
//        trackerNode.jobTracker().isScheduling(request.getId());
//        // todo
//        rpcCtx.sendResponse(null);
//    }
//
//    @Override
//    public String interest() {
//        return IsSchedulingRequest.class.getName();
//    }
//
//}
