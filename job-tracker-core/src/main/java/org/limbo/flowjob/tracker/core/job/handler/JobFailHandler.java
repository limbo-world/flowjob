package org.limbo.flowjob.tracker.core.job.handler;

/**
 * @author Devil
 * @since 2021/8/24
 */
public interface JobFailHandler {

    void execute();

    boolean terminate();

}
