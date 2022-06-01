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

package org.limbo.flowjob.broker.core.events;

import org.limbo.flowjob.broker.core.events.Event;

import java.time.Instant;

/**
 * @author Brozen
 * @since 2021-08-26
 */
public class StringEvent extends Event<String> {

    private static final long serialVersionUID = -4144835630814266129L;

    public StringEvent(String source) {
        super(source);
    }

    public StringEvent(String id, String source, Instant timestamp) {
        super(id, source, timestamp);
    }

}
