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

package org.limbo.flowjob.worker.core.infrastructure;

import org.limbo.flowjob.worker.core.domain.Job;

/**
 * shell 脚本执行器
 *
 * @author Devil
 * @date 2021/6/29 4:51 下午
 */
public class ShellJobExecutor implements JobExecutor {

    @Override
    public void run(Job job) {
        // todo
    }

    @Override
    public String getName() {
        return "shell";
    }
}
