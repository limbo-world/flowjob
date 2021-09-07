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

package org.limbo.flowjob.tracker.core.evnets;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * 领域事件
 *
 * @author Brozen
 * @since 2021-08-25
 */
public class Event<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1091865930180190422L;

    /**
     * 事件ID，可能用于做消费时的负载均衡
     */
    @Setter
    @Getter
    private String id;

    /**
     * 事件源，事件触发的地方，可以是服务、领域等
     */
    @Getter
    private T source;

    @Getter
    @Setter
    private String tag;

    /**
     * 事件触发时的时间戳
     */
    @Getter
    private Instant timestamp;

    public Event(T source) {
        this(null, source, Instant.now());
    }

    public Event(String id, T source, Instant timestamp) {
        this.id = id;
        this.source = source;
        this.timestamp = timestamp;
    }

}
