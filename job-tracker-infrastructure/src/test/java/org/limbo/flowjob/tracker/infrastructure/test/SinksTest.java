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

package org.limbo.flowjob.tracker.infrastructure.test;

import org.junit.Test;
import org.limbo.flowjob.tracker.core.schedule.scheduler.NamedThreadFactory;
import org.limbo.flowjob.tracker.infrastructure.events.ReactorEventPublisher;
import org.limbo.flowjob.tracker.infrastructure.events.StringEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Brozen
 * @since 2021-08-26
 */
public class SinksTest {

    public static void main(String[] args) {
        new SinksTest().testReactorEventPublisher();
    }

    @Test
    public void testReactorEventPublisher() {
        ReactorEventPublisher publisher = new ReactorEventPublisher(1, NamedThreadFactory.newInstance("test"));
//        publisher.subscribe(e -> {
//            System.out.println(e.getSource() + " at " + Thread.currentThread().getName());
//        });

        for (int i = 0; i < 20; i++) {
            publisher.publish(new StringEvent("event" + i));
        }

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
    }




}
