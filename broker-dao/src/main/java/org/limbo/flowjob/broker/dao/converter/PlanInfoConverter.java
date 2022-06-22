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

import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;

/**
 * todo 这个移除 直接在plan里面处理
 *
 * @author Brozen
 * @since 2021-07-13
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PlanInfoConverter {

    @Setter(onMethod_ = @Inject)
    private ApplicationContext ac;

    public abstract PlanInfoEntity toEntity(PlanInfo planInfo);

    public abstract PlanInfo toDO(PlanInfoEntity entity);


//    @Override
//    protected PlanInfoEntity doForward(PlanInfo planInfo) {
//        PlanInfoEntity po = new PlanInfoEntity();
//
//        po.setPlanId(planInfo.getPlanId());
//        po.setVersion(planInfo.getVersion());
//        po.setDescription(planInfo.getDescription());
//
//        ScheduleOption scheduleOption = planInfo.getScheduleOption();
//        po.setScheduleType(scheduleOption.getScheduleType().type);
//        po.setScheduleStartAt(scheduleOption.getScheduleStartAt());
//        po.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
//        po.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
//        po.setScheduleCron(scheduleOption.getScheduleCron());
//        po.setJobs(JacksonUtils.toJSONString(planInfo.getDag().jobs()));
//
//        // 能够查询到info信息，说明未删除
//        po.setIsDeleted(false);
//
//        return po;
//    }
//
//
//    @Override
//    protected PlanInfo doBackward(PlanInfoEntity po) {
//        PlanInfo planInfo = planInfoBuilderFactory.builder()
//                .planId(po.getPlanId())
//                .version(po.getVersion())
//                .description(po.getDescription())
//                .scheduleOption(new ScheduleOption(
//                        ScheduleType.parse(po.getScheduleType()),
//                        po.getScheduleStartAt(),
//                        Duration.ofMillis(po.getScheduleDelay()),
//                        Duration.ofMillis(po.getScheduleInterval()),
//                        po.getScheduleCron(),
//                        po.getScheduleCronType(),
//                        po.getRetry()
//                ))
//                .jobs(JacksonUtils.parseObject(po.getJobs(), new TypeReference<List<Job>>() {}))
//                .build();
//
//        // 注入依赖
//        ac.getAutowireCapableBeanFactory().autowireBean(planInfo);
//
//        return planInfo;
//    }

}
