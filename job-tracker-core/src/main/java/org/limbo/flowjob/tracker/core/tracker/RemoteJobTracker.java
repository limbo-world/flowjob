package org.limbo.flowjob.tracker.core.tracker;

/**
 * 此类型所表示的JobTracker运行在其他JVM中，调用此JobTracker方法时，将通过RPC接口调用远程JobTracker提供的服务。
 * TODO
 *
 * @author Brozen
 * @since 2021-06-16
 */
public abstract class RemoteJobTracker implements JobTracker {

    /**
     * RemoteJobTracker不支持此方法。
     * @return {@inheritDoc}
     */
    @Override
    public DisposableJobTracker start() {
        throw new UnsupportedOperationException("Cannot start remote job tracker.");
    }

    /**
     * RemoteJobTracker不支持此方法。
     * @return {@inheritDoc}
     */
    @Override
    public JobTrackerLifecycle lifecycle() {
        throw new UnsupportedOperationException("Remote job tracker lifecycle is not supported yet.");
    }

}
