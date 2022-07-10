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

package org.limbo.flowjob.broker.core.worker.statistics;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * worker执行作业统计信息
 *
 * @author Brozen
 * @since 2021-05-28
 */
@Data
public class WorkerStatistics {

    /**
     * 对应worker的ID
     */
    private String workerId;

    /**
     * 作业下发到此worker的次数
     */
    private Long jobDispatchCount;

    /**
     * 最后一次向此worker下发作业成功的时间
     */
    private LocalDateTime latestDispatchTime;

}
