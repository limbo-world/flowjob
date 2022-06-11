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

package org.limbo.flowjob.broker.dao.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Converter;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanInfoBuilderFactory;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.utils.jackson.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Component
public class PlanInfoPOConverter extends Converter<PlanInfo, PlanInfoEntity> {

    @Autowired
    private ApplicationContext ac;

    @Autowired
    private PlanInfoBuilderFactory planInfoBuilderFactory;


    /**
     * {@link PlanInfo} -> {@link PlanInfoEntity}
     */
    @Override
    protected PlanInfoEntity doForward(PlanInfo planInfo) {
        PlanInfoEntity po = new PlanInfoEntity();

        po.setPlanId(planInfo.getPlanId());
        po.setVersion(planInfo.getVersion());
        po.setDescription(planInfo.getDescription());

        ScheduleOption scheduleOption = planInfo.getScheduleOption();
        po.setScheduleType(scheduleOption.getScheduleType().type);
        po.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        po.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        po.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        po.setScheduleCron(scheduleOption.getScheduleCron());
        po.setJobs(JacksonUtils.toJSONString(planInfo.getDag().jobs()));

        // 能够查询到info信息，说明未删除
        po.setIsDeleted(false);

        return po;
    }


    /**
     * {@link PlanInfoEntity} -> {@link PlanInfo}
     */
    @Override
    protected PlanInfo doBackward(PlanInfoEntity po) {
        PlanInfo planInfo = planInfoBuilderFactory.builder()
                .planId(po.getPlanId())
                .version(po.getVersion())
                .description(po.getDescription())
                .scheduleOption(new ScheduleOption(
                        ScheduleType.parse(po.getScheduleType()),
                        po.getScheduleStartAt(),
                        Duration.ofMillis(po.getScheduleDelay()),
                        Duration.ofMillis(po.getScheduleInterval()),
                        po.getScheduleCron(),
                        po.getScheduleCronType(),
                        po.getRetry()
                ))
                .jobs(JacksonUtils.parseObject(po.getJobs(), new TypeReference<List<Job>>() {}))
                .build();

        // 注入依赖
        ac.getAutowireCapableBeanFactory().autowireBean(planInfo);

        return planInfo;
    }

}
