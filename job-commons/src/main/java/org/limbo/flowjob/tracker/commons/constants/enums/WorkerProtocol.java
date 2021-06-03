package org.limbo.flowjob.tracker.commons.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * worker服务的通信协议
 */
public enum WorkerProtocol {

    /**
     * HTTP协议通信
     */
    HTTP(1);

    @JsonValue
    public final byte protocol;

    WorkerProtocol(int protocol) {
        this(((byte) protocol));
    }

    WorkerProtocol(byte protocol) {
        this.protocol = protocol;
    }

    /**
     * 解析worker支持的协议
     */
    @JsonCreator
    public static WorkerProtocol parse(Number protocol) {
        if (protocol == null) {
            return null;
        }

        for (WorkerProtocol workerProtocol : values()) {
            if (protocol.byteValue() == workerProtocol.protocol) {
                return workerProtocol;
            }
        }

        return null;
    }

}