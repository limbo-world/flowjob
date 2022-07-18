//package org.limbo.flowjob.broker.cluster.single;
//
//import org.limbo.flowjob.broker.api.dto.BrokerDTO;
//import org.limbo.flowjob.broker.cluster.AbstractTrackerNode;
//import org.limbo.flowjob.broker.cluster.JobTrackerFactory;
//import org.limbo.flowjob.broker.core.node.DisposableTrackerNode;
//import org.limbo.flowjob.broker.core.node.WorkerManager;
//
//import javax.annotation.PostConstruct;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author Devil
// * @since 2021/8/12
// */
//public class SingleTrackerNode extends AbstractTrackerNode {
//
//    public SingleTrackerNode(String host, int port, JobTrackerFactory jobTrackerFactory, WorkerManager workerManager) {
//        super(host, port, jobTrackerFactory, workerManager);
//    }
//
//    @PostConstruct
//    @Override
//    public DisposableTrackerNode start() {
//        // 注册 JobTracker
//        beforeStart();
//        jobTracker = jobTrackerFactory.single();
//        return super.start();
//    }
//
//    @Override
//    public List<BrokerDTO> getNodes() {
//        return Collections.singletonList(getNodeInfo());
//    }
//
//}
