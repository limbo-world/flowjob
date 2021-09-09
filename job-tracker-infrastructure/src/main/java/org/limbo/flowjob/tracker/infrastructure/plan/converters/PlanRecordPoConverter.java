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
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.dao.po.PlanRecordPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class PlanRecordPoConverter extends Converter<PlanRecord, PlanRecordPO> {

    @Autowired
    private PlanRepository planRepository;

    /**
     * {@link PlanRecord} -> {@link PlanRecordPO}
     */
    @Override
    protected PlanRecordPO doForward(PlanRecord record) {
        PlanRecordPO po = new PlanRecordPO();
        po.setPlanId(record.getPlanId());
        po.setPlanRecordId(record.getPlanRecordId());
        po.setVersion(record.getVersion());
        po.setState(record.getState().status);
        po.setRetry(record.getRetry());
        po.setManual(record.isManual());
        po.setStartAt(TimeUtil.toLocalDateTime(record.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(record.getStartAt()));
        return po;
    }


    /**
     * {@link PlanInstancePO} -> {@link PlanInstance}
     */
    @Override
    protected PlanRecord doBackward(PlanRecordPO po) {
        Plan plan = planRepository.getPlan(po.getPlanId(), po.getVersion());

        PlanRecord record = new PlanRecord();
        record.setPlanId(po.getPlanId());
        record.setPlanRecordId(po.getPlanRecordId());
        record.setVersion(po.getVersion());
        record.setState(PlanScheduleStatus.parse(po.getState()));
        record.setDag(plan.getDag());
        record.setRetry(po.getRetry());
        record.setManual(po.getManual());
        record.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        record.setEndAt(TimeUtil.toInstant(po.getStartAt()));
        return record;
    }

}
