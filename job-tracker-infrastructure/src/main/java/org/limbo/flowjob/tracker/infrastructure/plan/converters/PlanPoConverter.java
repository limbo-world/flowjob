/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.infrastructure.plan.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Component
public class PlanPoConverter extends Converter<Plan, PlanPO> {

    /**
     * 作业触发计算器工厂
     */
    @Autowired
    private ScheduleCalculatorFactory scheduleCalculatorFactory;


    /**
     * {@link Plan} -> {@link PlanPO}
     */
    @Override
    protected PlanPO doForward(Plan plan) {
        PlanPO po = new PlanPO();

        po.setPlanId(plan.getPlanId());
        po.setPlanDesc(plan.getPlanDesc());

        ScheduleOption scheduleOption = plan.getScheduleOption();
        po.setScheduleType(scheduleOption.getScheduleType().type);
        po.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        po.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        po.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        po.setScheduleCron(scheduleOption.getScheduleCron());

        return po;
    }


    /**
     * {@link PlanPO} -> {@link Plan}
     */
    @Override
    protected Plan doBackward(PlanPO po) {

        // 先生成一个代理calculator，用于初始化JobDO
        ScheduleType scheduleType = ScheduleType.parse(po.getScheduleType());
        ScheduleCalculator scheduleCalculator = scheduleCalculatorFactory.newStrategy(scheduleType);

        Plan plan = new Plan(scheduleCalculator);
        plan.setPlanId(po.getPlanId());
        plan.setPlanDesc(po.getPlanDesc());

        plan.setScheduleOption(new ScheduleOption(
                scheduleType,
                po.getScheduleStartAt(),
                Duration.ofMillis(po.getScheduleDelay()),
                Duration.ofMillis(po.getScheduleInterval()),
                po.getScheduleCron()
        ));

        return plan;
    }

}
