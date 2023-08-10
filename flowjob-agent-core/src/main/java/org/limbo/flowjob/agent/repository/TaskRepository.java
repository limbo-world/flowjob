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

package org.limbo.flowjob.agent.repository;

import org.limbo.flowjob.agent.FlowjobConnectionFactory;
import org.limbo.flowjob.agent.Task;
import org.limbo.flowjob.api.constants.TaskType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/8/3
 */
public class TaskRepository {

    private FlowjobConnectionFactory connectionFactory;

    public TaskRepository(FlowjobConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void initTable() throws Exception {

    }

    public Task getById(String id) {
        return null;
    }

    public List<Task> getByJobInstanceId(String jobId, TaskType type, Long startId, Integer limit) {
        return null;
    }

    public List<Map<String, Object>> getAllTaskResult(String jobId, TaskType type) {
        return null;
    }

    public long countUnSuccess(String jobId, TaskType type) {
        return 0L;
    }

    public boolean batchSave(Collection<Task> tasks) {
        return true;
    }

    public boolean executing(Task task) {
        return true;
    }

    public boolean success(String taskId, String context, String result) {
        return true;
    }

    public boolean fail(String taskId, String errorMsg, String errorStack) {
        return true;
    }

}
