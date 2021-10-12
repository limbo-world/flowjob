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

package org.limbo.flowjob.tracker.core.job.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.tracker.commons.constants.enums.TaskType;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;

import java.io.Serializable;

/**
 * 任务信息
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = -9097550508482197117L;

    private String planId;

    private Long planRecordId;

    private Integer planInstanceId;

    private String jobId;

    private Integer jobInstanceId;

    private String taskId;

    /**
     * 任务类型
     */
    private TaskType type;

    /**
     * 作业属性，不可变。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     */
    private Attributes attributes;

    // -------- 非 po 属性

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private ExecutorOption executorOption;

}
