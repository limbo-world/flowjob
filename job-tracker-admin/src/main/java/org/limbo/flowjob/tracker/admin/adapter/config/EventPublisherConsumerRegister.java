package org.limbo.flowjob.tracker.admin.adapter.config;

import org.limbo.flowjob.tracker.core.job.consumer.*;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.context.JobRecordRepository;
import org.limbo.flowjob.tracker.core.job.context.TaskRepository;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;
import org.limbo.flowjob.tracker.infrastructure.events.ReactorEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 解决循环依赖的中转bean
 *
 * @author Devil
 * @since 2021/9/10
 */
@Component
public class EventPublisherConsumerRegister {

    @Autowired
    private ReactorEventPublisher eventPublisher;

    @Autowired
    private TrackerNode trackerNode;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanRecordRepository planRecordRepository;

    @Autowired
    private PlanInstanceRepository planInstanceRepository;

    @Autowired
    private JobRecordRepository jobRecordRepository;

    @Autowired
    private JobInstanceRepository jobInstanceRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkerManager workerManager;

    @PostConstruct
    public void subscribe() {
        // plan 下发
        eventPublisher.subscribe(new PlanDispatchConsumer(
                planRecordRepository,
                eventPublisher));
        // plan record 下发
        eventPublisher.subscribe(new PlanRecordDispatchConsumer(
                planInstanceRepository,
                jobRecordRepository,
                eventPublisher));
        // job record 下发
        eventPublisher.subscribe(new JobRecordDispatchConsumer(
                jobInstanceRepository,
                eventPublisher));
        // task 下发
        eventPublisher.subscribe(new TaskDispatchConsumer(
                jobRecordRepository,
                jobInstanceRepository,
                taskRepository,
                workerManager));
        // task 完成
        eventPublisher.subscribe(new ClosedConsumer(
                taskRepository,
                planRecordRepository,
                planInstanceRepository,
                planRepository,
                jobRecordRepository,
                jobInstanceRepository,
                trackerNode,
                eventPublisher));
    }

}
