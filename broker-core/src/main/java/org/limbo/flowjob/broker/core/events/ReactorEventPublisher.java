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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.utils.tuple.Tuple3;
import org.limbo.utils.tuple.Tuples;
import org.limbo.utils.verifies.Verifies;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author Brozen
 * @since 2021-08-25
 */
@Slf4j
public class ReactorEventPublisher extends Flux<Event<?>> implements EventPublisher<Event<?>>, CorePublisher<Event<?>> {

    /**
     * 用于负载均衡的发布者
     */
    private final List<Tuple3<Sinks.Many<Event<?>>, Scheduler, Flux<Event<?>>>> publishers;

    /**
     * 事件发布失败时的处理器
     */
    @Setter
    private Sinks.EmitFailureHandler publishFailHandler;

    public ReactorEventPublisher(int concurrency, int bufferSize, ThreadFactory threadFactory) {
        Verifies.verify(concurrency > 0, "concurrency must be a positive integer");
        Verifies.notNull(threadFactory, "threadFactory must not be null");

        this.publishers = new ArrayList<>(concurrency);

        for (int i = 0; i <concurrency; i++) {
            Sinks.Many<Event<?>> publisher = Sinks.many().multicast().onBackpressureBuffer(bufferSize);
            this.publishers.add(Tuples.of(
                    publisher, Schedulers.newSingle(threadFactory), publisher.asFlux()
            ));
        }

    }

    /**
     * {@inheritDoc}
     * @param event 事件对象
     */
    @Override
    public void publish(Event<?> event) {

        // FIXME 需不需要抽象出loaderBalancer
        int publishAt = RandomUtils.nextInt(0, publishers.size() - 1);
        String id = event.getId();
        if (StringUtils.isNotBlank(id)) {
            publishAt = id.hashCode() % publishers.size();
        }

        // 如未设置失败处理器，则默认3次
        Sinks.EmitFailureHandler failureHandler = publishFailHandler != null ? publishFailHandler : EmitFailureHandlers
                .signals()
                .results()
                .retry(3)
                .onRetry((s, r) -> {
                    if (log.isDebugEnabled()) {
                        log.trace("emit event {} failed, signal={} result={}, will retry", event, s, r);
                    }
                })
                .end();
        publishers.get(publishAt).getA().emitNext(event, failureHandler);

    }


    /**
     * 作为publisher时
     * {@inheritDoc}
     * @param subscriber
     */
    @Override
    public void subscribe(@Nonnull CoreSubscriber<? super Event<?>> subscriber) {
        CoreSubscriber<? super Event<?>> multiUpstreamSubscriber;
        if (subscriber instanceof Fuseable.ConditionalSubscriber) {
            @SuppressWarnings("unchecked")
            Fuseable.ConditionalSubscriber<? super Event<?>> s = (Fuseable.ConditionalSubscriber<? super Event<?>>) subscriber;
            multiUpstreamSubscriber = new ConditionalMultiUpstreamSubscriber(s);
        } else {
            multiUpstreamSubscriber = new MultiUpstreamSubscriber(subscriber);
        }

        for (Tuple3<Sinks.Many<Event<?>>, Scheduler, Flux<Event<?>>> tuple : publishers) {
            tuple.getA().asFlux()
                    .publishOn(tuple.getB())
                    .subscribe(multiUpstreamSubscriber);
        }
    }


    /**
     * 多个上游数据源的订阅者代理
     * FIXME 第一个上游done之后，其他上游发送的next事件无法被传达
     * FIXME upstreamSubscriptions非同步集合，可能有并发问题，改成CopyOnWriteArrayList
     */
    static class MultiUpstreamSubscriber implements CoreSubscriber<Event<?>>, Subscription {

        private final CoreSubscriber<? super Event<?>> actual;

        private final List<Subscription> upstreamSubscriptions;

        private final AtomicBoolean downstreamOnSubscribeNotified = new AtomicBoolean(false);

        static final AtomicLongFieldUpdater<MultiUpstreamSubscriber> REQUESTED =
                AtomicLongFieldUpdater.newUpdater(MultiUpstreamSubscriber.class, "requested");
        private volatile long requested;

        private boolean done = false;


        public MultiUpstreamSubscriber(CoreSubscriber<? super Event<?>> actual) {
            this.actual = actual;
            this.upstreamSubscriptions = new LinkedList<>();
        }

        @Override
        public void onSubscribe(@Nonnull Subscription s) {
            this.upstreamSubscriptions.add(s);

            if (downstreamOnSubscribeNotified.compareAndSet(false, true)) {
                this.actual.onSubscribe(this);
            } else {
                s.request(requested);
            }
        }

        @Override
        public void onNext(Event<?> event) {
            if (this.done) {
                Operators.onNextDropped(event, this.actual.currentContext());
            } else {
                this.actual.onNext(event);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (this.done) {
                Operators.onErrorDropped(t, this.actual.currentContext());
            } else {
                this.done = true;
                this.actual.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.actual.onComplete();
            }
        }

        @Override
        public void request(long n) {
            if (!Operators.validate(n)) {
                return;
            }

            // 如果request已经达到上限，没有继续增加，则不再继续向上游请求
            long prevValue = Operators.addCap(REQUESTED, this, n);
            if (requested != prevValue) {
                this.upstreamSubscriptions.forEach(s -> s.request(n));
            }
        }

        @Override
        public void cancel() {
            this.upstreamSubscriptions.forEach(Subscription::cancel);
        }
    }


    static class ConditionalMultiUpstreamSubscriber implements Fuseable.ConditionalSubscriber<Event<?>>, Subscription {

        private final Fuseable.ConditionalSubscriber<? super Event<?>> actual;

        private final List<Subscription> upstreamSubscriptions;

        private final AtomicBoolean downstreamOnSubscribeNotified = new AtomicBoolean(false);

        static final AtomicLongFieldUpdater<ConditionalMultiUpstreamSubscriber> REQUESTED =
                AtomicLongFieldUpdater.newUpdater(ConditionalMultiUpstreamSubscriber.class, "requested");
        private volatile long requested;

        private boolean done = false;


        public ConditionalMultiUpstreamSubscriber(Fuseable.ConditionalSubscriber<? super Event<?>> actual) {
            this.actual = actual;
            this.upstreamSubscriptions = new LinkedList<>();
        }

        @Override
        public void onSubscribe(@Nonnull Subscription s) {
            this.upstreamSubscriptions.add(s);

            if (downstreamOnSubscribeNotified.compareAndSet(false, true)) {
                this.actual.onSubscribe(this);
            } else {
                s.request(requested);
            }
        }

        @Override
        public void onNext(Event<?> event) {
            if (this.done) {
                Operators.onNextDropped(event, this.actual.currentContext());
            } else {
                this.actual.onNext(event);
            }
        }

        @Override
        public boolean tryOnNext(@Nonnull Event<?> event) {
            if (this.done) {
                Operators.onNextDropped(event, this.actual.currentContext());
                return true;
            } else {
                return this.actual.tryOnNext(event);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (this.done) {
                Operators.onErrorDropped(t, this.actual.currentContext());
            } else {
                this.done = true;
                this.actual.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.actual.onComplete();
            }
        }

        @Override
        public void request(long n) {
            if (!Operators.validate(n)) {
                return;
            }

            // 如果request已经达到上限，没有继续增加，则不再继续向上游请求
            long prevValue = Operators.addCap(REQUESTED, this, n);
            if (requested != prevValue) {
                this.upstreamSubscriptions.forEach(s -> s.request(n));
            }
        }

        @Override
        public void cancel() {
            this.upstreamSubscriptions.forEach(Subscription::cancel);
        }
    }


}
