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
import org.limbo.flowjob.tracker.core.tracker.DisposableTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.ReactorTrackerNodeLifecycle;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.flowjob.tracker.core.tracker.single.SingleTrackerNode;

import java.util.Scanner;

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
        SingleTrackerNode lifecycle = new SingleTrackerNode("", 100, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                lifecycle.beforeStart().subscribe(t -> {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("1" + t);
                }, System.err::println, () -> System.out.println("OK"));

                System.out.println(Thread.currentThread().getName() + " subscribe");
                sleep(10000);
            }
        }, "Thread-1").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                lifecycle.beforeStart().subscribe(t -> {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("2" + t);
                });

                System.out.println(Thread.currentThread().getName() + " subscribe");
                sleep(10000);
            }
        }, "Thread-2").start();

        // sleep 为了 在订阅后 再发布信息
        sleep(1000);
        lifecycle.start();

        new Scanner(System.in).next();
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
