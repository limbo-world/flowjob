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
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class PlanInstancePoConverter extends Converter<PlanInstance, PlanInstancePO> {

    /**
     * {@link PlanInstance} -> {@link PlanInstancePO}
     */
    @Override
    protected PlanInstancePO doForward(PlanInstance planInstance) {
        PlanInstancePO po = new PlanInstancePO();
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
     * {@link PlanInstancePO} -> {@link PlanInstance}
     */
    @Override
    protected PlanInstance doBackward(PlanInstancePO po) {
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
