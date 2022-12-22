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

package org.limbo.flowjob.broker.test.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.junit.jupiter.api.Test;
import org.limbo.flowjob.broker.core.schedule.scheduler.NamedThreadFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Devil
 * @since 2022/6/21
 */
public class TimeTest {

    @Test
    public void test() {
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(NamedThreadFactory.newInstance(this.getClass().getSimpleName() + "-timer-"));
        hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.println(new Date());
            }
        }, 1, TimeUnit.SECONDS);
    }
}
