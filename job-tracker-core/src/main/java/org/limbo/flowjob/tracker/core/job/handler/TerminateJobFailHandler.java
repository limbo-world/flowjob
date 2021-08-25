package org.limbo.flowjob.tracker.core.job.handler;

/**
 * @author Devil
 * @since 2021/8/24
 */
public class TerminateJobFailHandler implements JobFailHandler {
    @Override
    public void execute() {
    }

    @Override
    public boolean terminate() {
        return true;
    }
}
