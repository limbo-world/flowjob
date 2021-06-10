package org.limbo.flowjob.tracker.commons.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * worker服务的通信协议
 */
public enum WorkerProtocol {

    /**
     * HTTP协议通信
     */
    HTTP(10, "http", 80),

    /**
     * HTTPS协议
     */
    HTTPS(11, "https", 443),

    /**
     * RSocket通信协议
     */
    R_SOCKET(20, "rs", 7136),

    /**
     * RSocket SSL通信协议
     */
    R_SOCKETS(21, "rss", 7137),

    ;

    /**
     * 协议类型值
     */
    @JsonValue
    public final byte protocol;

    /**
     * 协议名称
     */
    public final String name;

    /**
     * 协议默认端口号
     */
    public final int port;

    WorkerProtocol(int protocol, String name, int port) {
        this(((byte) protocol), name, port);
    }

    WorkerProtocol(byte protocol, String name, int port) {
        this.protocol = protocol;
        this.name = name;
        this.port = port;
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

    /**
     * 解析worker支持的协议
     */
    @JsonCreator
    public static WorkerProtocol parse(String protocolName) {
        if (StringUtils.isBlank(protocolName)) {
            return null;
        }

        for (WorkerProtocol workerProtocol : values()) {
            if (workerProtocol.name.equalsIgnoreCase(protocolName)) {
                return workerProtocol;
            }
        }

        return null;
    }

}