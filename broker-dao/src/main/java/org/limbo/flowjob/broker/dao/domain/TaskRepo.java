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

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class TaskRepo implements TaskRepository {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    /**
     * {@inheritDoc}
     *
     * @param task 作业执行实例
     * @return
     */
    @Override
    @Transactional
    public String save(Task task) {
        Verifies.notNull(task, "task can't be null");
        TaskEntity entity = DomainConverter.toTaskEntity(task);
        taskEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    @Transactional
    public void saveAll(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        Map<Task, TaskEntity> map = new HashMap<>();
        for (Task task : tasks) {
            map.put(task, DomainConverter.toTaskEntity(task));
        }

        taskEntityRepo.saveAll(map.values());
        taskEntityRepo.flush();

        for (Map.Entry<Task, TaskEntity> entry : map.entrySet()) {
            entry.getKey().setTaskId(String.valueOf(entry.getValue().getId()));
        }
    }

    @Override
    public Task get(String taskId) {
        return taskEntityRepo.findById(Long.valueOf(taskId)).map(entity -> DomainConverter.toTask(entity, planInfoEntityRepo)).orElse(null);
    }

}
