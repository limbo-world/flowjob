package org.limbo.flowjob.tracker.core.tracker.election.rpc.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.api.dto.broker.BrokerDTO;
import org.limbo.flowjob.tracker.core.tracker.election.ElectionTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.request.NodeInfoRequest;

/**
 * @author Devil
 * @since 2021/8/11
 */
public class NodeInfoProcessor implements RpcProcessor<NodeInfoRequest> {

    private ElectionTrackerNode trackerNode;

    public NodeInfoProcessor(ElectionTrackerNode trackerNode) {
        this.trackerNode = trackerNode;
    }

    @Override
    public void handleRequest(RpcContext rpcCtx, NodeInfoRequest request) {
        BrokerDTO node = new BrokerDTO();
        node.setHost(trackerNode.getHost());
        node.setPort(trackerNode.getPort());

        rpcCtx.sendResponse(ResponseDTO.<BrokerDTO>builder().ok(node).build());
    }

    @Override
    public String interest() {
        return NodeInfoRequest.class.getName();
    }

}
