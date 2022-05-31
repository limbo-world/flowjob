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

package org.limbo.flowjob.tracker.core.plan;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;

import java.time.Instant;

/**
 * 计划实例
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class PlanInstance {

    /**
     * 计划ID
     */
    private ID id;

    /**
     * 状态
     */
    private PlanScheduleStatus state;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;


    /**
     * 计划ID抽象
     */
    public static class ID {

        public final String planId;

        public final Long planRecordId;

        public final Integer planInstanceId;

        public ID(String planId, Long planRecordId, Integer planInstanceId) {
            this.planId = planId;
            this.planRecordId = planRecordId;
            this.planInstanceId = planInstanceId;
        }
    }

}
