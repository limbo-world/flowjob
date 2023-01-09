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
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.limbo.flowjob.broker.test.support.PlanParamFactory;
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
    @Transactional
    void addFixedRate() {
        PlanParam param = PlanParamFactory.newFixedRateAddParam();

        String id = planService.save(null, param);
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
        PlanParam param = PlanParamFactory.newFixedRateAddParam();
        String id = planService.save(null, param);
        id = planService.save(id, PlanParamFactory.newFixedRateReplaceParam());
    }

    @Test
    void get() {
//        Plan plan = planService.get("2");
//        log.info("plan>>>>>{}", JacksonUtils.toJSONString(plan));
//        Assertions.assertNotNull(plan, "");
    }
}
