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

package org.limbo.flowjob.worker.core.executor;

import lombok.Getter;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;

/**
 * @author Brozen
 * @since 2022-09-06
 */
public abstract class AbstractTaskExecutor implements TaskExecutor {

    /**
     * 执行器名称
     */
    @Getter
    private final String name;

    /**
     * 执行器描述
     */
    @Getter
    private String description;

    /**
     * 执行器类型
     */
    @Getter
    private JobExecuteType type;


    /**
     * 初始化执行器参数，子类调用
     */
    protected AbstractTaskExecutor(String name, String description, JobExecuteType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

}
