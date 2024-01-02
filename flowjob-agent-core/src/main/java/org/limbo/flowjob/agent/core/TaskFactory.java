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

package org.limbo.flowjob.agent.core;

import org.limbo.flowjob.agent.core.entity.Job;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.LocalDateTimeUtils;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/8/8
 */
public class TaskFactory {

    public static final LocalDateTime DEFAULT_REPORT_TIME = LocalDateTimeUtils.parse("2000-01-01 00:00:00", Formatters.YMD_HMS);

    public static Task create(String id, Job job, Object taskAttributes, TaskType type, Worker worker) {
        return Task.builder()
                .id(id)
                .jobId(job.getId())
                .type(type)
                .executorName(job.getExecutorName())
                .status(TaskStatus.SCHEDULING)
                .context(job.getContext())
                .jobAttributes(job.getAttributes())
                .taskAttributes(taskAttributes == null ? "" : JacksonUtils.toJSONString(taskAttributes))
                .worker(worker)
                .lastReportAt(DEFAULT_REPORT_TIME)
                .triggerAt(TimeUtils.currentLocalDateTime())
                .build();
    }

}
