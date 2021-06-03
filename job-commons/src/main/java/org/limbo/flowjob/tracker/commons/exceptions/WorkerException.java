package org.limbo.flowjob.tracker.commons.exceptions;

/**
 * Worker调用时发生的异常
 *
 * @author Brozen
 * @since 2021-05-25
 */
public class WorkerException extends RuntimeException {

    /**
     * 异常的worker id
     */
    private String workerId;

    public WorkerException(String workerId, String message) {
        super(message);
        this.workerId = workerId;
    }

    public WorkerException(String workerId, String message, Throwable cause) {
        super(message, cause);
        this.workerId = workerId;
    }

}
