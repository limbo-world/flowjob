/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.api.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
        if (protocolName == null) {
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
