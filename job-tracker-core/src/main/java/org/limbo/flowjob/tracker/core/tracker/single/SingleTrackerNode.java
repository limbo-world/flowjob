package org.limbo.flowjob.tracker.core.tracker.single;

import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNodeDto;
import org.limbo.flowjob.tracker.core.tracker.AbstractTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.DisposableTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.JobTrackerFactory;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;

import java.util.Collections;
import java.util.List;

/**
 * @author Devil
 * @since 2021/8/12
 */
public class SingleTrackerNode extends AbstractTrackerNode {

    public SingleTrackerNode(String hostname, int port, JobTrackerFactory jobTrackerFactory, WorkerManager workerManager) {
        super(hostname, port, jobTrackerFactory, workerManager);
    }

    @Override
    public DisposableTrackerNode start() {
        // 注册 JobTracker
        beforeStart().subscribe(t -> jobTracker = jobTrackerFactory.single());
        return super.start();
    }

    @Override
    public List<TrackerNodeDto> getNodes() {
        return Collections.singletonList(getNodeInfo());
    }

}
