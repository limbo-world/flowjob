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

package org.limbo.flowjob.tracker.core.tracker;

import reactor.core.Disposable;

/**
 * 持有JobTracker的上下文信息，并提供用于关闭JobTracker的非阻塞API。
 *
 * @author Brozen
 * @since 2021-05-17
 */
public interface DisposableTrackerNode extends Disposable {

    /**
     * 底层JobTracker
     */
    TrackerNode node();

}
