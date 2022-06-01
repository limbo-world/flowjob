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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.locks.LockSupport;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public class ReactorTest {

    @Test
    public void testReplayProcessor() {
        ReplayProcessor<Integer> processor = ReplayProcessor.create();

        Mono.create(sink -> processor
                .filter(i -> i % 2 == 0)
                .subscribe(sink::success, sink::error, sink::success)
        ).subscribe(System.out::println);

        Mono.create(sink -> processor
                .filter(i -> i % 2 == 1)
                .subscribe(sink::success, sink::error, sink::success)
        ).subscribe(System.out::println);

        for (int i = 0; i < 10; i++) {
            processor.onNext(i);
        }
    }


    @Test
    public void testSinks() {
        Sinks.One<Integer> one = Sinks.one();

        one.emitValue(10, (signalType, emitResult) -> {
            System.out.println(signalType);
            System.out.println(emitResult);
            return false;
        });

        one.asMono().subscribe(System.out::println);
        one.asMono().subscribe(System.out::println);

    }

    @Test
    public void subscribeOn() throws InterruptedException {
        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> {
                    System.out.println("map before " + Thread.currentThread().getName());
                    return "10" + i;
                })
                .subscribeOn(Schedulers.newSingle("Scheduler"))
                .map(i -> {
                    System.out.println("map after " + Thread.currentThread().getName());
                    return "value " + i;
                }).take(1);

        Thread t = new Thread(() -> {
            flux.subscribe(s1 -> {
                System.out.println(s1);
                System.out.println("subscribe " + Thread.currentThread().getName());
            });
        });

        t.start();
        t.join();
        LockSupport.park();
    }

}
