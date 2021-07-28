package org.limbo.flowjob.tracker.core.plan;

import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.StrategyFactory;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;

/**
 * @author Devil
 * @since 2021/7/26
 */
public class PlanBuilderFactory {

    private StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory;

    private Executor<Plan> executor;

    public PlanBuilderFactory(StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory, Executor<Plan> executor) {
        this.strategyFactory = strategyFactory;
        this.executor = executor;
    }

    public Plan.Builder Builder() {
        return new Plan.Builder(strategyFactory, executor);
    }

}
