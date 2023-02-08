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

package org.limbo.flowjob.worker.core.executor;


import org.limbo.flowjob.worker.core.domain.MapTask;
import org.limbo.flowjob.worker.core.domain.Task;

import java.util.List;
import java.util.Map;

/**
 * 任务执行器
 *
 * @author Devil
 * @since 2021/7/24
 */
public interface MapTaskExecutor extends TaskExecutor {

    @Override
    default void run(Task task) {
        switch (task.getType()) {
            case SPLIT:
                task.setResult(split(task));
                break;
            case MAP:
                task.setResult(map((MapTask) task));
                break;
            default:
                break;
        }
    }

    /**
     * 切分创建多个子task
     * @param task 任务执行上下文
     */
    List<Map<String, Object>> split(Task task);

    /**
     * 处理map分片任务
     * @param task 任务执行上下文
     */
    Map<String, Object> map(MapTask task);

}
