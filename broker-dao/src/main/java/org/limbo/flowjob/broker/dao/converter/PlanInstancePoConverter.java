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

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceContextEntity;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class PlanInstancePoConverter extends Converter<PlanInstance, PlanInstanceContextEntity> {

    /**
     * {@link PlanInstance} -> {@link PlanInstanceContextEntity}
     */
    @Override
    protected PlanInstanceContextEntity doForward(PlanInstance planInstance) {
        PlanInstanceContextEntity po = new PlanInstanceContextEntity();
        PlanInstance.ID instanceId = planInstance.getId();
        po.setPlanId(instanceId.planId);
        po.setPlanRecordId(instanceId.planRecordId);
        po.setPlanInstanceId(instanceId.planInstanceId);
        po.setState(planInstance.getState().status);
        po.setStartAt(TimeUtil.toLocalDateTime(planInstance.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(planInstance.getStartAt()));
        return po;
    }


    /**
     * {@link PlanInstanceContextEntity} -> {@link PlanInstance}
     */
    @Override
    protected PlanInstance doBackward(PlanInstanceContextEntity po) {
        PlanInstance planInstance = new PlanInstance();
        PlanInstance.ID instanceId = new PlanInstance.ID(
                po.getPlanId(),
                po.getPlanRecordId(),
                po.getPlanInstanceId()
        );
        planInstance.setId(instanceId);
        planInstance.setState(PlanScheduleStatus.parse(po.getState()));
        planInstance.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        planInstance.setEndAt(TimeUtil.toInstant(po.getEndAt()));
        return planInstance;
    }

}
