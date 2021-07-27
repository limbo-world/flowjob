package org.limbo.flowjob.tracker.core.plan;

import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.StrategyFactory;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;

import java.util.List;

/**
 * @author Devil
 * @since 2021/7/26
 */
public class PlanFactory {

    private StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory;

    private Executor<Plan> executor;

    public PlanFactory(StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory, Executor<Plan> executor) {
        this.strategyFactory = strategyFactory;
        this.executor = executor;
    }

    /**
     * 想改成builder+factory
     *
     * @param planId
     * @param version
     * @param planDesc
     * @param scheduleOption
     * @param jobs
     * @return
     */
    public Plan create(String planId, Integer version, String planDesc, ScheduleOption scheduleOption, List<Job> jobs) {
        ScheduleType scheduleType = scheduleOption.getScheduleType();
        ScheduleCalculator scheduleCalculator = strategyFactory.newStrategy(scheduleType);

        Plan plan = new Plan(scheduleCalculator, executor);
        plan.setPlanId(planId);
        plan.setVersion(version);
        plan.setPlanDesc(planDesc);
        plan.setScheduleOption(scheduleOption);
        plan.setJobs(jobs);
        return plan;
    }

}
