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

package org.limbo.flowjob.tracker.core.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;

/**
 * 执行器配置，值对象
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Data
@Setter(AccessLevel.NONE)
public class ExecutorOption {

    /**
     * 执行器名称
     */
    private String name;

    /**
     * 执行器类型
     */
    private JobExecuteType type;

    @JsonCreator
    public ExecutorOption(@JsonProperty("name") String name,
                          @JsonProperty("type") JobExecuteType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * 设置名称
     */
    public ExecutorOption setName(String name) {
        return new ExecutorOption(name, type);
    }


    /**
     * 设置类型
     */
    public ExecutorOption setType(JobExecuteType type) {
        return new ExecutorOption(name, type);
    }

}
