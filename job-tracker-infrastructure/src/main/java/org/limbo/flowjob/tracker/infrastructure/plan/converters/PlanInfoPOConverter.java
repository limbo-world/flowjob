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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanBuilderFactory;
import org.limbo.flowjob.tracker.dao.po.PlanInfoPO;
import org.limbo.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Component
public class PlanInfoPOConverter extends Converter<Plan, PlanInfoPO> {

    @Autowired
    private PlanBuilderFactory planBuilderFactory;


    /**
     * {@link Plan} -> {@link PlanInfoPO}
     */
    @Override
    protected PlanInfoPO doForward(Plan plan) {
        PlanInfoPO po = new PlanInfoPO();

        po.setPlanId(plan.getPlanId());
        po.setVersion(plan.getVersion());
        po.setPlanDesc(plan.getPlanDesc());

        ScheduleOption scheduleOption = plan.getScheduleOption();
        po.setScheduleType(scheduleOption.getScheduleType().type);
        po.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        po.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        po.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        po.setScheduleCron(scheduleOption.getScheduleCron());
        po.setJobs(JacksonUtils.toJSONString(plan.getJobs()));

        return po;
    }


    /**
     * {@link PlanInfoPO} -> {@link Plan}
     */
    @Override
    protected Plan doBackward(PlanInfoPO po) {
        return planBuilderFactory.Builder()
                .planId(po.getPlanId())
                .version(po.getVersion())
                .planDesc(po.getPlanDesc())
                .scheduleOption(new ScheduleOption(
                        ScheduleType.parse(po.getScheduleType()),
                        po.getScheduleStartAt(),
                        Duration.ofMillis(po.getScheduleDelay()),
                        Duration.ofMillis(po.getScheduleInterval()),
                        po.getScheduleCron()
                ))
                .jobs(JacksonUtils.parseObject(po.getJobs(), new TypeReference<List<Job>>() {
                }))
                .build();
    }

}
