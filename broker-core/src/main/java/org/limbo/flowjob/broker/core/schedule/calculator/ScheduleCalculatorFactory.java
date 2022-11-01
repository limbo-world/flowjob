package org.limbo.flowjob.broker.core.schedule.calculator;

import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;

import java.util.function.Function;

/**
 * 重新定义泛型
 *
 * @author Brozen
 * @since 2021-10-19
 */
public interface ScheduleCalculatorFactory extends Function<ScheduleType, ScheduleCalculator> {


}
