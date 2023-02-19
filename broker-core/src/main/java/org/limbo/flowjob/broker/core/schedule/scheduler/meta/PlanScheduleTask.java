package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.strategy.IPlanScheduleStrategy;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.time.LocalDateTime;

/**
 * @author pengqi
 * @date 2023/1/9
 */
@Slf4j
public class PlanScheduleTask extends LoopMetaTask {

    @Getter
    private final Plan plan;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final IPlanScheduleStrategy iScheduleStrategy;

    public PlanScheduleTask(Plan plan, LocalDateTime lastTriggerAt, LocalDateTime lastFeedbackAt,
                            IPlanScheduleStrategy iScheduleStrategy, MetaTaskScheduler metaTaskScheduler) {
        super(lastTriggerAt, lastFeedbackAt, plan.getScheduleOption(), metaTaskScheduler);
        this.plan = plan;
        this.iScheduleStrategy = iScheduleStrategy;
    }


    @Override
    public void execute() {
        if (plan == null) {
            log.error("{} plan is null", scheduleId());
            return;
        }
        if (TriggerType.SCHEDULE != plan.getTriggerType()) {
            return;
        }
        ScheduleOption scheduleOption = getScheduleOption();
        if (scheduleOption == null || scheduleOption.getScheduleType() == null || ScheduleType.UNKNOWN == scheduleOption.getScheduleType()) {
            log.error("{} scheduleType is {} scheduleOption={}", scheduleId(), MsgConstants.UNKNOWN, scheduleOption);
            return;
        }
        switch (getScheduleOption().getScheduleType()) {
            case FIXED_RATE:
            case CRON:
                executeFixedRate();
                break;
            default:
                // FIXED_DELAY 交由执行完后处理
                break;
        }
    }

    @Override
    protected void executeTask() {
        iScheduleStrategy.schedule(TriggerType.SCHEDULE, plan, getTriggerAt());
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN;
    }

    @Override
    public String getMetaId() {
        return plan.getPlanId() + "-" + plan.getVersion();
    }
}
