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

package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;
import org.limbo.flowjob.broker.core.repositories.PlanSchedulerRepository;
import org.limbo.flowjob.broker.dao.converter.PlanInfoConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanInfoMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Devil
 * @since 2022/6/20
 */
@ApplicationScoped
public class PlanSchedulerRepo implements PlanSchedulerRepository {

    @Setter(onMethod_ = @Inject)
    private PlanInfoMapper planInfoMapper;

    @Setter(onMethod_ = @Inject)
    private PlanInfoConverter planInfoConverter;

    @Override
    public PlanScheduler get(String version) {
        PlanInfoEntity planInfoEntity = planInfoMapper.selectOne(Wrappers
                .<PlanInfoEntity>lambdaQuery()
                .eq(PlanInfoEntity::getPlanInfoId, version)
        );

        PlanInfo planInfo = planInfoConverter.toDO(planInfoEntity);

        PlanScheduler planScheduler = new PlanScheduler();
        planScheduler.setInfo(planInfo);
//        planScheduler.setLastScheduleAt(null); // todo
//        planScheduler.setLastFeedbackAt(null);
        return planScheduler;
    }

    @Override
    public List<Plan> listSchedulablePlans() {
        throw new UnsupportedOperationException();
    }

}
