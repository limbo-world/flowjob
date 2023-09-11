package org.limbo.flowjob.worker.core.domain;

/**
 * @author OttO
 * @since 2023/9/11
 */
public class WorkerContext {

    private static volatile String workerId = "";

    public static void setWorkerId(String workerId) {
        workerId = workerId;
    }

    public static String getWorkerId() {
        return workerId;
    }

}
