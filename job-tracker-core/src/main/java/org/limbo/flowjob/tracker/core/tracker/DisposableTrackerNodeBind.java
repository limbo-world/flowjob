package org.limbo.flowjob.tracker.core.tracker;

/**
 * 用于关闭JobTracker的实现。
 * @author Devil
 * @since 2021/8/12
 */
public class DisposableTrackerNodeBind implements DisposableTrackerNode {

    private final TrackerNode trackerNode;

    public DisposableTrackerNodeBind(TrackerNode trackerNode) {
        this.trackerNode = trackerNode;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public TrackerNode node() {
        return trackerNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (trackerNode.isStopped()) {
            return;
        }

        trackerNode.stop();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public boolean isDisposed() {
        return trackerNode.isStopped();
    }
}
