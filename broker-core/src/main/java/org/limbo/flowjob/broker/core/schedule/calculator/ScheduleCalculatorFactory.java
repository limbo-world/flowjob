package org.limbo.flowjob.broker.core.schedule.calculator;

import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.utils.strategies.StrategyFactory;
import org.limbo.flowjob.broker.core.schedule.Schedulable;

/**
 * 重新定义泛型
 *
 * @author Brozen
 * @since 2021-10-19
 */
public interface ScheduleCalculatorFactory extends StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> {


}
