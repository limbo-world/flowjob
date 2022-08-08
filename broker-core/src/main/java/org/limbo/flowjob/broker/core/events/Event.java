/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.events;

import lombok.Getter;
import org.limbo.flowjob.common.utils.UUIDUtils;

import java.io.Serializable;

/**
 * 领域事件
 *
 * @author Brozen
 * @since 2021-08-25
 */
public class Event implements Serializable {

    private static final long serialVersionUID = -1091865930180190422L;

    /**
     * 事件ID，可能用于做消费时的负载均衡
     */
    @Getter
    private final String id;

    /**
     * 事件主题，可以用来区分事件
     */
    @Getter
    private final EventTopic topic;

    /**
     * 事件源，事件触发的地方，可以是服务、领域等
     */
    @Getter
    private final Object source;

    public Event(Object source, EventTopic topic) {
        this(UUIDUtils.randomID(), source, topic);
    }

    public Event(String id, Object source, EventTopic topic) {
        this.id = id;
        this.source = source;
        this.topic = topic;
    }

}
