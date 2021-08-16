/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.tracker.commons.dto.job;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
public class JobInstanceDto {

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 计划实例的ID
     */
    private Long planInstanceId;

    /**
     * 作业ID planId + planInstanceId + jobId 全局唯一
     */
    private String jobId;

    /**
     * plan 版本
     */
    private Integer version;

    /**
     * 执行器的名称
     */
    private String executorName;

    /**
     * 执行时候的参数
     */
    private String executorParam;

    /**
     * 作业上下文元数据
     */
    private Map<String, List<String>> attributes;

}
