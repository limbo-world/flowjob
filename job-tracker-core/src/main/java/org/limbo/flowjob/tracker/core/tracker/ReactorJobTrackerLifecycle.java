package org.limbo.flowjob.tracker.core.tracker;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * @author Brozen
 * @since 2021-06-01
 */
public class ReactorJobTrackerLifecycle implements JobTrackerLifecycle {

    /**
     * 用于触发JobTracker声明周期事件
     */
    private Sinks.Many<JobTrackerLifecycleEvent> eventSink;

    public ReactorJobTrackerLifecycle() {
        eventSink = Sinks.many().multicast().directAllOrNothing();
    }

    /**
     * 触发启动前生命周期事件
     * @param disposableJobTracker 可用于关闭JobTracker
     */
    protected void triggerBeforeStart(DisposableJobTracker disposableJobTracker) {
        JobTrackerLifecycleEvent event = new JobTrackerLifecycleEvent(
                disposableJobTracker, JobTrackerLifecycleEventType.BEFORE_START);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 触发启动后生命周期事件
     * @param disposableJobTracker 可用于关闭JobTracker
     */
    protected void triggerAfterStart(DisposableJobTracker disposableJobTracker) {
        JobTrackerLifecycleEvent event = new JobTrackerLifecycleEvent(
                disposableJobTracker, JobTrackerLifecycleEventType.AFTER_START);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 触发关闭前生命周期事件
     * @param jobTracker 被关闭的JobTracker
     */
    protected void triggerBeforeStop(JobTracker jobTracker) {
        JobTrackerLifecycleEvent event = new JobTrackerLifecycleEvent(
                jobTracker, JobTrackerLifecycleEventType.BEFORE_STOP);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 触发启动后生命周期事件
     * @param jobTracker 被关闭的JobTracker
     */
    protected void triggerAfterStop(JobTracker jobTracker) {
        JobTrackerLifecycleEvent event = new JobTrackerLifecycleEvent(
                jobTracker, JobTrackerLifecycleEventType.AFTER_STOP);
        eventSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<DisposableJobTracker> beforeStart() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == JobTrackerLifecycleEventType.BEFORE_START)
                .map(e -> ((DisposableJobTracker) e.source)));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<DisposableJobTracker> afterStart() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == JobTrackerLifecycleEventType.AFTER_START)
                .map(e -> ((DisposableJobTracker) e.source)));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobTracker> beforeStop() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == JobTrackerLifecycleEventType.BEFORE_STOP)
                .map(e -> ((JobTracker) e.source)));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobTracker> afterStop() {
        return Mono.from(eventSink.asFlux()
                .filter(e -> e.type == JobTrackerLifecycleEventType.AFTER_STOP)
                .map(e -> ((JobTracker) e.source)));
    }

    /**
     * JobTracker生命周期事件对象
     */
    public static class JobTrackerLifecycleEvent {

        /**
         * 对于不同的生命周期事件，事件源不同
         */
        private Object source;

        private JobTrackerLifecycleEventType type;

        public JobTrackerLifecycleEvent(Object source, JobTrackerLifecycleEventType type) {
            this.source = source;
            this.type = type;
        }
    }
}
