package org.limbo.flowjob.tracker.commons.exceptions;

import lombok.Getter;

/**
 * @author Brozen
 * @since 2021-05-21
 */
public class JobContextException extends JobExecuteException {

    /**
     * 作业上下文ID
     */
    @Getter
    private String jobContextId;

    public JobContextException(String jobId, String jobContextId, String message) {
        super(jobId, message);
        this.jobContextId = jobContextId;
    }

    public JobContextException(String jobId, String jobContextId, String message, Throwable cause) {
        super(jobId, message, cause);
        this.jobContextId = jobContextId;
    }

}
