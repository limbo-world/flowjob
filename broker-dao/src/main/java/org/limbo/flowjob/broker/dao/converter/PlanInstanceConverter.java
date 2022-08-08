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
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class PlanInstanceConverter extends Converter<PlanInstance, PlanInstanceEntity> {

    /**
     * {@link PlanInstance} -> {@link PlanInstanceEntity}
     */
    @Override
    protected PlanInstanceEntity doForward(PlanInstance record) {
        // todo
        return null;
//        PlanInstanceEntity po = new PlanInstanceEntity();
//        PlanInstance.ID recordId = record.getId();
//        po.setPlanInstanceId(recordId.planId + recordId.planRecordId);
//        po.setPlanInfoId(recordId.planId + record.getVersion());
//        po.setState(record.getState().status);
//        po.setRetry(record.getRetry());
//        po.setManual(record.isManual());
//        po.setStartAt(TimeUtil.toLocalDateTime(record.getStartAt()));
//        po.setEndAt(TimeUtil.toLocalDateTime(record.getStartAt()));
//        return po;
    }

    @Override
    protected PlanInstance doBackward(PlanInstanceEntity po) {
        // todo
        return null;
//        PlanInfoEntity po = mapper.selectById(planInfoId);
//        return PlanInfoPOConverter.reverse().convert(po);

//        PlanInstance record = new PlanInstance();
//        PlanInstance.ID recordId = new PlanInstance.ID(
//                po.getPlanId(),
//                po.getPlanRecordId()
//        );
//        record.setId(recordId);
//        record.setVersion(po.getVersion());
//        record.setState(PlanScheduleStatus.parse(po.getState()));
//        record.setDag(planInfo.getDag());
//        record.setRetry(po.getRetry());
//        record.setManual(po.getManual());
//        record.setStartAt(TimeUtil.toInstant(po.getStartAt()));
//        record.setEndAt(TimeUtil.toInstant(po.getStartAt()));
//        return record;
    }

}
