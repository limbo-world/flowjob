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


import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.domain.SubTask;
import org.limbo.flowjob.worker.core.domain.Task;

import java.util.List;
import java.util.Map;

/**
 * 任务执行器
 *
 * @author Devil
 * @since 2021/7/24
 */
public abstract class MapReduceTaskExecutor implements TaskExecutor {

    @Override
    public void run(Task task) {
        switch (task.getType()) {
            case SHARDING:
                sharding(task);
                break;
            case MAP:
                Map<String, Object> result = map(task);
                task.setResult(result == null ? "" : JacksonUtils.toJSONString(result));
                break;
            case REDUCE:
                reduce(task);
                break;
            default:
                throw new IllegalArgumentException("wrong task type task: " + JacksonUtils.toJSONString(task));
        }
    }

    /**
     * 切分创建多个子task
     *
     * @param task 任务
     */
    public abstract void sharding(Task task);

    /**
     * 业务需要在执行线程中使用此方法，如果放到线程池中调用需要自己处理
     */
    protected Boolean submitSubTasks(Task task, List<SubTask> subTasks) {
        if (CollectionUtils.isEmpty(subTasks)) {
            throw new IllegalArgumentException("sub task empty");
        }
        int maxSize = 100;
        if (subTasks.size() > maxSize) {
            throw new IllegalArgumentException("sub task size > " + maxSize);
        }
        ExecuteContext executeContext = ThreadLocalContext.getExecuteContext();
        return executeContext.getAgentRpc().submitSubTasks(task, subTasks);
    }

    /**
     * 处理map分片任务
     *
     * @param task 任务
     */
    public abstract Map<String, Object> map(Task task);

    /**
     * 处理reduce任务
     *
     * @param task 任务
     */
    public abstract void reduce(Task task);

}
