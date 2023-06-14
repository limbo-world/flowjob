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

package org.limbo.flowjob.broker.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.application.schedule.ScheduleStrategy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * @author Devil
 * @since 2023/5/11
 */
@Tag(name = "plan instance console api")
@RestController
public class PlanInstanceController {

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategy scheduleStrategy;

    /**
     * api 触发对应planInstanceId下的job
     */
    @Operation(summary = "触发对应job调度")
    @PostMapping("/api/v1/plan-instance/{planInstanceId}/job/{jobId}/schedule")
    public ResponseDTO<Void> scheduleJob(@Validated @NotNull(message = "no planInstanceId") @PathVariable("planInstanceId") String planInstanceId,
                                         @Validated @NotNull(message = "no jobId") @PathVariable("jobId") String jobId) {
        scheduleStrategy.manualScheduleJob(planInstanceId, jobId);
        return ResponseDTO.<Void>builder().ok().build();
    }

}
