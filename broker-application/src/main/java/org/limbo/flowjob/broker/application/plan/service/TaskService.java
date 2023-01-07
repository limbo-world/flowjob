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

package org.limbo.flowjob.broker.application.plan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskManager;
import org.limbo.flowjob.broker.core.domain.task.TaskResult;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.ExecuteResult;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/1/5
 */
@Slf4j
@Service
public class TaskService implements TaskManager {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    /**
     * Worker任务执行反馈
     *
     * @param taskId 任务id
     * @param param  反馈参数
     */
    @Transactional
    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        ExecuteResult result = ExecuteResult.parse(param.getResult());
        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        TaskEntity taskEntity = taskEntityRepo.findById(taskId).orElse(null);
        Verifies.notNull(taskEntity, "task is null");

        Task task = domainConverter.toTask(taskEntity);

        switch (result) {
            case SUCCEED:
                task.success(param.getResultAttributes());
                break;

            case FAILED:
                task.fail(param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

    @Override
    public List<TaskResult> getTaskResults(String jobInstanceId, TaskType taskType) {
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(jobInstanceId, taskType.type);
        if (CollectionUtils.isEmpty(taskEntities)) {
            return Collections.emptyList();
        }
        return taskEntities.stream().map(taskEntity -> {
            TaskResult taskResult = TaskResult.builder()
                    .taskId(taskEntity.getTaskId())
                    .errorMsg(taskEntity.getErrorMsg())
                    .errorStackTrace(taskEntity.getErrorStackTrace())
                    .build();
            switch (taskType) {
                case SPLIT:
                    taskResult.setSubTaskAttributes(JacksonUtils.parseObject(taskEntity.getResult(), new TypeReference<List<Map<String, Object>>>() {
                    }));
                    break;
                case MAP:
                    taskResult.setResultAttributes(JacksonUtils.parseObject(taskEntity.getResult(), new TypeReference<Map<String, Object>>() {
                    }));
                    break;
                default:
                    break;
            }
            return taskResult;
        }).collect(Collectors.toList());
    }
}
