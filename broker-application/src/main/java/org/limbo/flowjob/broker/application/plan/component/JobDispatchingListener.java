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

package org.limbo.flowjob.broker.application.plan.component;

import org.limbo.flowjob.broker.core.events.EventTopic;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.application.plan.support.EventListener;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2022/8/5
 */
@Component
public class JobDispatchingListener implements EventListener {

    @Override
    public EventTopic topic() {
        return JobStatus.DISPATCHING;
    }

    @Override
    public void accept(Event event) {
        JobInstance jobInstance = (JobInstance) event.getSource();
        // todo 进行check 是否有job已经失败并需要终止plan 如果是则无需下发，修改状态为失败 原因为---由别的job失败导致无需下发
    }

}
