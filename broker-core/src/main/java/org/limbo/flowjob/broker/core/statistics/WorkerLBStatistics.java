/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.limbo.flowjob.common.lb.LBServerStatistics;

import java.time.Instant;

/**
 * @author Brozen
 * @since 2022-12-21
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class WorkerLBStatistics implements LBServerStatistics {

    /**
     * Worker ID
     */
    private final String workerId;

    /**
     * 上次下发任务时间
     */
    private final Instant lastDispatchTaskAt;

    /**
     * 下发任务次数
     */
    private final int dispatchTimes;


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getServerId() {
        return workerId;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Instant getLatestAccessAt() {
        return lastDispatchTaskAt;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public int getAccessTimes() {
        return dispatchTimes;
    }

}
