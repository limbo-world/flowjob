package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.strategy.IPlanScheduleStrategy;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author pengqi
 * @date 2023/1/9
 */
@Slf4j
public class PlanScheduleTask extends LoopMetaTask {

    @Getter
    @JsonIgnore
    private final Plan plan;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
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
        super.execute();
    }

    @Override
    protected void executeFixedDelay() {
        executeTask();
        metaTaskScheduler.unschedule(scheduleId());
    }

    @Override
    protected void executeTask() {
        if (ScheduleType.FIXED_DELAY == getScheduleOption().getScheduleType()) {
            iScheduleStrategy.schedule(TriggerType.SCHEDULE, plan, getNextTriggerAt());
        } else {
            iScheduleStrategy.schedule(TriggerType.SCHEDULE, plan, getLastTriggerAt());
        }
    }

    @Override
    public LocalDateTime calNextTriggerAt() {
        LocalDateTime triggerAt = super.calNextTriggerAt();
        return triggerAt.truncatedTo(ChronoUnit.SECONDS);  // 这里获取到的是毫秒 转为秒
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
