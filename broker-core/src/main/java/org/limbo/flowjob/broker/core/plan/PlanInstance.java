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

package org.limbo.flowjob.broker.core.plan;

import lombok.Data;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class PlanInstance implements Serializable {

    private static final long serialVersionUID = 1837382860200548371L;

    private ID id;

    /**
     * 计划的版本
     */
    private Integer version;

    /**
     * 计划调度状态
     */
    private PlanScheduleStatus state;

    /**
     * 重试次数
     */
    private Integer retry;

    /**
     * 是否手动下发
     */
    private boolean manual;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    // ===== 非 po 属性

    private JobDAG dag;

    public PlanInstanceContext newInstance(PlanInstanceContext.ID planInstanceId, PlanScheduleStatus state) {
        PlanInstanceContext instance = new PlanInstanceContext();
        instance.setId(planInstanceId);
        instance.setState(state);
        instance.setStartAt(TimeUtil.nowInstant());
        return instance;
    }


    /**
     * 作业执行记录ID抽象
     */
    public static class ID {

        public final String planId;

        public final Long planRecordId;

        public ID(String planId, Long planRecordId) {
            this.planId = planId;
            this.planRecordId = planRecordId;
        }
    }

}
