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
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.job.JobInfo;
import org.limbo.flowjob.broker.core.plan.job.dag.DAG;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * todo 这个移除 直接在plan里面处理
 *
 * @author Brozen
 * @since 2021-07-13
 */
@Component
public class PlanInfoConverter {

    public PlanInfoEntity toEntity(PlanInfo planInfo) {
        PlanInfoEntity entity = new PlanInfoEntity();

        entity.setPlanId(Long.valueOf(planInfo.getPlanId()));
        entity.setDescription(planInfo.getDescription());

        ScheduleOption scheduleOption = planInfo.getScheduleOption();
        entity.setScheduleType(scheduleOption.getScheduleType().type);
        entity.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        entity.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        entity.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        entity.setScheduleCron(scheduleOption.getScheduleCron());
        entity.setJobs(JacksonUtils.toJSONString(planInfo.getDag().nodes()));

        // 能够查询到info信息，说明未删除
        entity.setIsDeleted(false);

        return entity;
    }

    public PlanInfo toDO(PlanInfoEntity entity) {
        return new PlanInfo(String.valueOf(entity.getPlanId()), String.valueOf(entity.getId()), entity.getDescription(), new ScheduleOption(
                ScheduleType.parse(entity.getScheduleType()),
                TriggerType.parse(entity.getTriggerType()),
                entity.getScheduleStartAt(),
                Duration.ofMillis(entity.getScheduleDelay()),
                Duration.ofMillis(entity.getScheduleInterval()),
                entity.getScheduleCron(),
                entity.getScheduleCronType()
        ), new DAG<>(JacksonUtils.parseObject(entity.getJobs(), new TypeReference<List<JobInfo>>() {
        })));
    }

}
