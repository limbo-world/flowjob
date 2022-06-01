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

package org.limbo.flowjob.broker.core.broker;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * @author Brozen
 * @since 2021-06-01
 */
public class ReactorTrackerNodeLifecycle implements TrackerNodeLifecycle {

    /**
     * 用于触发JobTracker声明周期事件
     */
    private Sinks.Many<TrackerNodeLifecycleEvent> eventSink;

    public ReactorTrackerNodeLifecycle() {
        eventSink = Sinks.many().multicast().directAllOrNothing();
    }

    /**
     * 触发启动前生命周期事件
     * @param node 可用于关闭 node
     */
    protected void triggerBeforeStart(DisposableTrackerNode node) {
        TrackerNodeLifecycleEvent event = new TrackerNodeLifecycleEvent(
                node, TrackerNodeLifecycleEventType.BEFORE_START);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 触发启动后生命周期事件
     * @param node 可用于关闭 node
     */
    protected void triggerAfterStart(DisposableTrackerNode node) {
        TrackerNodeLifecycleEvent event = new TrackerNodeLifecycleEvent(
                node, TrackerNodeLifecycleEventType.AFTER_START);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 触发关闭前生命周期事件
     * @param node 被关闭的 node
     */
    protected void triggerBeforeStop(TrackerNode node) {
        TrackerNodeLifecycleEvent event = new TrackerNodeLifecycleEvent(
                node, TrackerNodeLifecycleEventType.BEFORE_STOP);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 触发启动后生命周期事件
     * @param node 被关闭的 node
     */
    protected void triggerAfterStop(TrackerNode node) {
        TrackerNodeLifecycleEvent event = new TrackerNodeLifecycleEvent(
                node, TrackerNodeLifecycleEventType.AFTER_STOP);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<DisposableTrackerNode> beforeStart() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == TrackerNodeLifecycleEventType.BEFORE_START)
                .map(e -> ((DisposableTrackerNode) e.source)));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<DisposableTrackerNode> afterStart() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == TrackerNodeLifecycleEventType.AFTER_START)
                .map(e -> ((DisposableTrackerNode) e.source)));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobTracker> beforeStop() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == TrackerNodeLifecycleEventType.BEFORE_STOP)
                .map(e -> ((JobTracker) e.source)));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobTracker> afterStop() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == TrackerNodeLifecycleEventType.AFTER_STOP)
                .map(e -> ((JobTracker) e.source)));
    }

    /**
     * JobTracker生命周期事件对象
     */
    public static class TrackerNodeLifecycleEvent {

        /**
         * 对于不同的生命周期事件，事件源不同
         */
        private Object source;

        private TrackerNodeLifecycleEventType type;

        public TrackerNodeLifecycleEvent(Object source, TrackerNodeLifecycleEventType type) {
            this.source = source;
            this.type = type;
        }
    }
}
