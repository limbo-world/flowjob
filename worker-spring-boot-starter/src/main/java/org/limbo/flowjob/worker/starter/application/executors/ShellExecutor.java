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

package org.limbo.flowjob.worker.starter.application.executors;

import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.executor.TaskExecutor;

import java.io.IOException;

/**
 * 内置的 Shell 执行器，执行下发的任务中的脚本，以 bash 方式执行
 *
 * @author Brozen
 * @since 2022-09-11
 */
public class ShellExecutor implements TaskExecutor {

    private static final String NAME = "bashShell";

    /**
     * {@inheritDoc}
     * @param context 任务执行上下文
     */
    @Override
    public void run(ExecuteContext context) {
        Task task = context.getTask();
        String shellName = task.getExecutorName();
        Object scriptObj = task.getContext().get("script");

        if (scriptObj == null || !(scriptObj instanceof String)) {
            throw new VerifyException("\"script\" is null or not a String");
        }
        String script = (String) scriptObj;
        Verifies.notBlank(script, "\"script\" is blank");

        String finalScript = shellName + "\n" + script;

        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(finalScript);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getDescription() {
        return "Executing shell script with bash";
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public JobExecuteType getType() {
        return JobExecuteType.SHELL;
    }

}
