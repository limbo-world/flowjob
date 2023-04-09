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

package org.limbo.flowjob.broker.application.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventPublisher;
import org.limbo.flowjob.broker.core.events.EventTopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步事件分发
 *
 * @author Brozen
 * @since 2021-08-25
 */
@Slf4j
public class EventProcessor implements EventPublisher {

    private final Map<EventTopic, List<EventListener>> subscribers = new HashMap<>();

    @Override
    public void publish(Event event) {
        List<EventListener> eventListeners = subscribers.get(event.getTopic());
        if (CollectionUtils.isNotEmpty(eventListeners)) {
            for (EventListener eventListener : eventListeners) {
                eventListener.accept(event);
            }
        }
    }

    /**
     * 注册订阅监听
     */
    public void subscribe(EventListener listener) {
        subscribers.computeIfAbsent(listener.topic(), planEventTopic -> new ArrayList<>()).add(listener);
    }

}
