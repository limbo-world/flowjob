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

package org.limbo.flowjob.broker.test.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.limbo.flowjob.api.param.PlanAddParam;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.test.support.PlanFactory;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2022/9/2
 */
@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class PlanServiceTest {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    @Test
//    @Transactional
    void addFixedRate() {
        PlanAddParam param = PlanFactory.newFixedRateAddParam();

        String id = planService.add(param);
        Plan plan = planService.get(id);
        log.debug(JacksonUtils.toJSONString(plan));
        log.info("plan>>>>>{}", JacksonUtils.toJSONString(plan));
        Assertions.assertNotNull(plan);
    }

    @Test
    @Transactional
    void enablePlan() {
        String planId = "2";

        boolean start = planService.start(planId);
        Assertions.assertTrue(start);
    }

    @Test
    @Transactional
    void replace() {
        PlanAddParam param = PlanFactory.newFixedRateAddParam();
        String id = planService.add(param);
        id = planService.replace(id, PlanFactory.newFixedRateReplaceParam());
        Plan plan = planService.get(id);
        log.info("plan>>>>>{}", JacksonUtils.toJSONString(plan));
        Assertions.assertNotNull(plan, "");
    }

    @Test
    void get() {
        Plan plan = planService.get("2");
        log.info("plan>>>>>{}", JacksonUtils.toJSONString(plan));
        Assertions.assertNotNull(plan, "");
    }
}
