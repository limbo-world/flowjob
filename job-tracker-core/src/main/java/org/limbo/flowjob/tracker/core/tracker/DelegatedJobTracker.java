package org.limbo.flowjob.tracker.core.tracker;

import lombok.experimental.Delegate;

/**
 * @author Brozen
 * @since 2021-06-16
 */
public class DelegatedJobTracker implements JobTracker {

    @Delegate
    private JobTracker tracker;

    public DelegatedJobTracker(JobTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * 重设被代理的JobTracker
     * @param tracker 实际被调用的tracker
     */
    public void setDelegatedTracker(JobTracker tracker) {
        this.tracker = tracker;
    }

}
