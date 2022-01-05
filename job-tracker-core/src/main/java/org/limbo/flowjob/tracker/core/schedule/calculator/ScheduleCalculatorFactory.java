package org.limbo.flowjob.tracker.core.schedule.calculator;

import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.StrategyFactory;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;

/**
 * 重新定义泛型
 *
 * @author Brozen
 * @since 2021-10-19
 */
public interface ScheduleCalculatorFactory extends StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> {


}
