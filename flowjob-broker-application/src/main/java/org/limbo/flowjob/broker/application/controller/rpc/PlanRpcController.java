/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.application.controller.rpc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.broker.PlanJobScheduleParam;
import org.limbo.flowjob.api.param.broker.PlanScheduleParam;
import org.limbo.flowjob.broker.core.schedule.SchedulerProcessor;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_PLAN_INSTANCE_JOB_SCHEDULE;
import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_PLAN_SCHEDULE;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Tag(name = "plan remote rpc")
@RestController
public class PlanRpcController {

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private SchedulerProcessor schedulerProcessor;

    /**
     * 手动触发对应 plan
     */
    @Operation(summary = "触发对应plan调度")
    @PostMapping(API_PLAN_SCHEDULE)
    public ResponseDTO<Void> schedulePlan(@RequestBody PlanScheduleParam param) {
        Attributes planAttribute = null;
        if (MapUtils.isEmpty(param.getAttributes())) {
            planAttribute = new Attributes(param.getAttributes());
        }
        schedulerProcessor.schedule(param.getPlanId(), TriggerType.API, planAttribute, TimeUtils.currentLocalDateTime());
        return ResponseDTO.<Void>builder().ok().build();
    }

    /**
     * api 触发对应planInstanceId下的job调度 目前只有workflow类型会用到
     */
    @Operation(summary = "触发对应job调度")
    @PostMapping(API_PLAN_INSTANCE_JOB_SCHEDULE)
    public ResponseDTO<Void> scheduleJob(@RequestBody PlanJobScheduleParam param) {
        schedulerProcessor.scheduleJob(param.getPlanInstanceId(), param.getJobId());
        return ResponseDTO.<Void>builder().ok().build();
    }


}
