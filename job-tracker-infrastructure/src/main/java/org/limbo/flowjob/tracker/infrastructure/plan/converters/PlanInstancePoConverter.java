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
        po.setPlanId(planInstance.getPlanId());
        po.setPlanInstanceId(planInstance.getPlanInstanceId());
        po.setVersion(planInstance.getVersion());
        po.setState(planInstance.getState().status);
        po.setReschedule(planInstance.isReschedule());
        po.setStartAt(planInstance.getStartAt() == null ? null : TimeUtil.toLocalDateTime(planInstance.getStartAt()));
        po.setEndAt(planInstance.getEndAt() == null ? null : TimeUtil.toLocalDateTime(planInstance.getStartAt()));
        return po;
    }


    /**
     * {@link PlanInstancePO} -> {@link PlanInstance}
     */
    @Override
    protected PlanInstance doBackward(PlanInstancePO po) {
        PlanInstance planInstance = new PlanInstance();
        planInstance.setPlanId(po.getPlanId());
        planInstance.setPlanInstanceId(po.getPlanInstanceId());
        planInstance.setVersion(po.getVersion());
        planInstance.setState(PlanScheduleStatus.parse(po.getState()));
        planInstance.setReschedule(po.getReschedule());
        planInstance.setStartAt(po.getStartAt() == null ? null : TimeUtil.toInstant(po.getStartAt()));
        planInstance.setEndAt(po.getEndAt() == null ? null : TimeUtil.toInstant(po.getEndAt()));
        return planInstance;
    }

}
