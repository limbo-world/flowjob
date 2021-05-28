package org.limbo.flowjob.tracker.commons.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Worker的状态
 */
public enum WorkerStatus {

    /**
     * Worker正常运行中
     */
    RUNNING(1),

    /**
     * Worker熔断中，此状态的Worker无法接受任务，并将等待心跳重连并复活。
     */
    FUSING(2),

    /**
     * Worker已停止。
     */
    TERMINATED(3),

    ;

    @JsonValue
    public final int status;

    WorkerStatus(int status) {
        this.status = status;
    }

    /**
     * 解析worker状态
     */
    @JsonCreator
    public static WorkerStatus parse(Integer status) {
        if (status == null) {
            return null;
        }

        for (WorkerStatus workerStatus : values()) {
            if (status.equals(workerStatus.status)) {
                return workerStatus;
            }
        }

        return null;
    }
}