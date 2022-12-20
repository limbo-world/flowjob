/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2022-10-11
 */
public abstract class FixDelayMetaTask extends LoopMetaTask {

    protected FixDelayMetaTask(Duration interval, MetaTaskScheduler metaTaskScheduler) {
        this(TimeUtils.currentLocalDateTime(), Duration.ZERO, interval, metaTaskScheduler);
    }


    protected FixDelayMetaTask(LocalDateTime triggerAt, Duration delay, Duration interval, MetaTaskScheduler metaTaskScheduler) {
        super(triggerAt, new ScheduleOption(ScheduleType.FIXED_DELAY, null, delay, interval, null, null), metaTaskScheduler);
    }

}
