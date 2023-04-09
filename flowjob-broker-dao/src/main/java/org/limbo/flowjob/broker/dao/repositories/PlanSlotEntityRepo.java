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

import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Devil
 * @since 2022/8/23
 */
public interface PlanSlotEntityRepo extends JpaRepository<PlanSlotEntity, Long> {

    List<PlanSlotEntity> findBySlotIn(List<Integer> slots);

    PlanSlotEntity findByPlanId(String planId);

}
