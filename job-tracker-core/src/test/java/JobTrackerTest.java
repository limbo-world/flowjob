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

import org.junit.Test;
import org.limbo.flowjob.tracker.core.tracker.DisposableJobTracker;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.ReactorJobTrackerLifecycle;

/**
 * @author Brozen
 * @since 2021-06-01
 */
public class JobTrackerTest {

    /**
     * 测试下，Sinks.many().multicast().directAllOrNothing(); 只有在Subscriber订阅之后发射的信号才会有效，因为没有缓存。
     */
    @Test
    public void testJobTrackerLifecycle() {
        ReactorJobTrackerLifecycle lifecycle = new ReactorJobTrackerLifecycle();
        lifecycle.beforeStart().subscribe(t -> System.out.println("1" + t), System.err::println, () -> System.out.println("OK"));

//        lifecycle.triggerBeforeStart(new DisposableJobTracker() {
//            @Override
//            public JobTracker jobTracker() {
//                return null;
//            }
//
//            @Override
//            public void dispose() {
//
//            }
//        });
        lifecycle.beforeStart().subscribe(t -> System.out.println("2" + t));

    }

}
