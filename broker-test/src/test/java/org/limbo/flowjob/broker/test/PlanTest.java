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

package org.limbo.flowjob.broker.test;

import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * @author Devil
 * @since 2022/6/21
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PlanTest {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Test
    public void insert() {
        PlanEntity plan = new PlanEntity();
        String planId = new Date().toString();
        plan.setId(planId);

        PlanEntity planEntity = planEntityRepo.saveAndFlush(plan);
        System.out.println(planEntity.getId());
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        System.out.println(planEntityOptional.get());
    }

    @Test
    @Transactional
//    @Rollback(false)
    public void update() {
        String id = "Wed Jun 22 21:00:44 CST 2022";
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(id);
        PlanEntity plan = planEntityOptional.get();
//        plan.setCurrentVersion(new Date().toString());

//        PlanEntity planEntity = planJpaRepo.saveAndFlush(plan);
//        System.out.println(planEntity);

        String newVersion = new Date().toString();
        System.out.println(planEntityRepo.updateVersion(newVersion, newVersion, id, plan.getCurrentVersion(), plan.getRecentlyVersion()));

        PlanEntity planEntity = planEntityRepo.findById(id).get();
        System.out.println(1);
    }
}
