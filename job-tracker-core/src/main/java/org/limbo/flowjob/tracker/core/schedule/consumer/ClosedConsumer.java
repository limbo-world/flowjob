package org.limbo.flowjob.tracker.core.schedule.consumer;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class ClosedConsumer implements Consumer<JobInstance> {

    private final JobInstanceRepository jobInstanceRepository;

    private final PlanInstanceRepository planInstanceRepository;

    private final PlanRepository planRepository;

    private final TrackerNode trackerNode;

    public ClosedConsumer(JobInstanceRepository jobInstanceRepository,
                          PlanInstanceRepository planInstanceRepository,
                          PlanRepository planRepository,
                          TrackerNode trackerNode) {
        this.jobInstanceRepository = jobInstanceRepository;
        this.planInstanceRepository = planInstanceRepository;
        this.planRepository = planRepository;
        this.trackerNode = trackerNode;
    }

    @Override
    public void accept(JobInstance jobInstance) {
        if (log.isDebugEnabled()) {
            log.debug(jobInstance.getWorkerId() + " closed " + jobInstance.getId());
        }
        jobInstanceRepository.updateInstance(jobInstance);

        // todo 判断 当前job是否为最终任务

        // 判断 plan 是否需要 feedback 只有 FIXED_INTERVAL类型需要反馈，让任务在时间轮里面能重新下发，手动的和其他的都不需要
        PlanInstance planInstance = planInstanceRepository.getInstance(jobInstance.getPlanId(), jobInstance.getPlanInstanceId());
        planInstance.setEndAt(Instant.now());
        planInstanceRepository.updateInstance(planInstance);

        Plan plan = planRepository.getPlan(planInstance.getPlanId(), planInstance.getVersion());
        plan.setLastScheduleAt(planInstance.getStartAt());
        plan.setLastFeedBackAt(planInstance.getEndAt());
        if (ScheduleType.FIXED_INTERVAL == plan.getScheduleOption().getScheduleType() && planInstance.isReschedule()) {
            trackerNode.jobTracker().schedule(plan);
        }
    }
}
