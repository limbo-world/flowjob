package org.limbo.flowjob.tracker.core.tracker;

import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;

/**
 * 主从模式下，从节点JobTracker。从节点接受到的worker请求，需要通过从节点，将请求转发到主节点上。
 * TODO
 * @author Brozen
 * @since 2021-06-16
 */
public class FollowerJobTracker extends LocalJobTracker {

    /**
     * 当前节点是从节点时，此属性代表主节点tracker
     */
    protected RemoteJobTracker leader;

    public FollowerJobTracker(WorkerRepository workerRepository) {
        super(workerRepository);
    }

    @Override
    public DisposableJobTracker start() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}
