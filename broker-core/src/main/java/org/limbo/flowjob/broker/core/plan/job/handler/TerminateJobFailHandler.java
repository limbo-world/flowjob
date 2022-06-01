package org.limbo.flowjob.broker.core.plan.job.handler;

/**
 * @author Devil
 * @since 2021/8/24
 */
public class TerminateJobFailHandler implements JobFailHandler {
    @Override
    public void handle() {
    }

    @Override
    public boolean terminate() {
        return true;
    }
}
