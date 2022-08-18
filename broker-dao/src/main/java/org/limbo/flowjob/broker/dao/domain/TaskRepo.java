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
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class TaskRepo implements TaskRepository {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerManager workerManager;
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
        TaskEntity entity = toEntity(task);
        taskEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public Task get(String taskId) {
        return taskEntityRepo.findById(Long.valueOf(taskId)).map(this::toDO).orElse(null);
    }

    private TaskEntity toEntity(Task task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setJobInstanceId(Long.valueOf(task.getJobInstanceId()));
        taskEntity.setJobId(task.getJobId());
        taskEntity.setStatus(task.getStatus().status);
        taskEntity.setWorkerId(Long.valueOf(task.getWorkerId()));
        taskEntity.setAttributes(task.getAttributes() == null ? "" : task.getAttributes().toString());
        taskEntity.setErrorMsg(task.getErrorMsg());
        taskEntity.setErrorStackTrace(task.getErrorStackTrace());
        taskEntity.setStartAt(task.getStartAt());
        taskEntity.setEndAt(task.getEndAt());
        taskEntity.setId(Long.valueOf(task.getTaskId()));
        return taskEntity;
    }

    private Task toDO(TaskEntity entity) {
        Task task = new Task();
        task.setTaskId(entity.getId().toString());
        task.setJobInstanceId(entity.getJobInstanceId().toString());
        task.setJobId(entity.getJobId());
        task.setStatus(TaskStatus.parse(entity.getStatus()));
        task.setWorkerId(String.valueOf(entity.getWorkerId()));
        task.setAttributes(new Attributes(entity.getAttributes()));
        task.setErrorMsg(entity.getErrorMsg());
        task.setErrorStackTrace(entity.getErrorStackTrace());
        task.setStartAt(entity.getStartAt());
        task.setEndAt(entity.getEndAt());
        task.setWorkerManager(workerManager);

        // job
        PlanInfoEntity planInfo = planInfoEntityRepo.findById(Long.valueOf(task.getPlanVersion())).get();
        DAG<JobInfo> jobInfoDAG = DomainConverter.toJobDag(planInfo.getJobs());
        JobInfo jobInfo = jobInfoDAG.getNode(entity.getJobId());
        task.setDispatchOption(jobInfo.getDispatchOption());
        task.setExecutorOption(jobInfo.getExecutorOption());
        return task;
    }

}
