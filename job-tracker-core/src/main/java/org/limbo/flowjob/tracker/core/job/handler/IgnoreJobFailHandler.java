package org.limbo.flowjob.tracker.core.job.handler;

/**
 * @author Devil
 * @since 2021/8/24
 */
public class IgnoreJobFailHandler implements JobFailHandler {
    @Override
    public void handle() {

    }

    @Override
    public boolean terminate() {
        return false;
    }

}
