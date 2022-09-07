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

package org.limbo.flowjob.worker.application.executors;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.executor.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/28
 */
@Slf4j
@Component
public class HelloExecutor implements TaskExecutor {


    @Override
    public void run(ExecuteContext context) {
        log.warn("Say hello to {}", context.getTask().getId());
    }

    @Override
    public String getName() {
        return "hello";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public JobExecuteType getType() {
        return JobExecuteType.FUNCTION;
    }
}
