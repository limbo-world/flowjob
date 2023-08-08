///*
// *
// *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
// *  *
// *  * Licensed under the Apache License, Version 2.0 (the "License");
// *  * you may not use this file except in compliance with the License.
// *  * You may obtain a copy of the License at
// *  *
// *  * 	http://www.apache.org/licenses/LICENSE-2.0
// *  *
// *  * Unless required by applicable law or agreed to in writing, software
// *  * distributed under the License is distributed on an "AS IS" BASIS,
// *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  * See the License for the specific language governing permissions and
// *  * limitations under the License.
// *
// */
//
//package org.limbo.flowjob.agent;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.limbo.flowjob.broker.core.domain.task.TaskManager;
//import org.limbo.flowjob.broker.core.domain.task.TaskResult;
//import org.limbo.flowjob.broker.dao.entity.TaskEntity;
//import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
//import org.limbo.flowjob.api.constants.TaskType;
//import org.limbo.flowjob.common.utils.json.JacksonUtils;
//import org.springframework.stereotype.Component;
//
//import javax.inject.Inject;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @author pengqi
// * @date 2023/1/9
// */
//@Slf4j
//@Component
//public class TaskManagerComponent implements TaskManager {
//
//    @Setter(onMethod_ = @Inject)
//    private TaskEntityRepo taskEntityRepo;
//
//    @Override
//    public List<TaskResult> getTaskResults(String jobInstanceId, TaskType taskType) {
//        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(jobInstanceId, taskType.type);
//        if (CollectionUtils.isEmpty(taskEntities)) {
//            return Collections.emptyList();
//        }
//        return taskEntities.stream().map(taskEntity -> {
//            TaskResult taskResult = TaskResult.builder()
//                    .taskId(taskEntity.getTaskId())
//                    .errorMsg(taskEntity.getErrorMsg())
//                    .errorStackTrace(taskEntity.getErrorStackTrace())
//                    .build();
//            switch (taskType) {
//                case SHARDING:
//                    taskResult.setSubTaskAttributes(JacksonUtils.parseObject(taskEntity.getResult(), new TypeReference<List<Map<String, Object>>>() {
//                    }));
//                    break;
//                case MAP:
//                    taskResult.setResultAttributes(JacksonUtils.parseObject(taskEntity.getResult(), new TypeReference<Map<String, Object>>() {
//                    }));
//                    break;
//                default:
//                    break;
//            }
//            return taskResult;
//        }).collect(Collectors.toList());
//    }
//}
