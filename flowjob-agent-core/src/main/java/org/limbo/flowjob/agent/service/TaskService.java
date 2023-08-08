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

package org.limbo.flowjob.agent.service;

import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.Task;
import org.limbo.flowjob.agent.repository.TaskRepository;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.util.List;

/**
 * @author Devil
 * @since 2023/8/4
 */
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * task 成功处理
     */
    // todo 事务
    public void taskSuccess(Task task, Object result) {
        boolean updated = taskRepository.success(task.getTaskId(), task.getContext().toString(), task.getJobAttributes().toString(), JacksonUtils.toJSONString(result));
        if (!updated) { // 已经被更新 无需重复处理
            return;
        }

        switch (task.getType()) {
            case STANDALONE:
            case REDUCE:
                rpcFeedbackJobSuccess();
                break;
            case SHARDING:
                // 将待下发的 task 进行异步下发

                break;
            case BROADCAST:
                // 检测是否所有task都已经完成
                // 如果已经完成 rpcFeedbackJobSuccess();
                break;
            case MAP:
                // 检测是否所有task都已经完成
                // 如果已经完成 下发 ReduceTask
                break;
        }

        // 检查task是否都已经完成
        List<Task> tasks = taskRepository.getByJobInstanceId(task.getJobInstanceId());
        boolean success = tasks.stream().allMatch(entity -> TaskStatus.SUCCEED == entity.getStatus());
        if (!success) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }
        // todo 聚合上下文内容
        Attributes context = new Attributes();
        for (Task t : tasks) {
            Attributes taskContext = new Attributes(t.getContext().toMap());
            context.put(taskContext);
        }
        // 判断当前 job 类型 进行后续处理
//        switch (task.getJobType()) {
//            case STANDALONE:
//            case BROADCAST:
//                rpcFeedbackJobSuccess();
//                break;
//            case MAP:
//                handleMapJobSuccess(task, jobInstance);
//                break;
//            case MAP_REDUCE:
//                handleMapReduceJobSuccess(task, jobInstance);
//                break;
//            default:
//                throw new IllegalArgumentException(MsgConstants.UNKNOWN + " JobType in jobInstance:" + jobInstance.getJobInstanceId());
//        }
    }

    public void rpcFeedbackJobSuccess() {

    }

    public void rpcFeedbackJobFail() {

    }


    /**
     * task失败处理
     */
    public void taskFail(Task task, String errorMsg, String errorStackTrace) {
        if (StringUtils.isBlank(errorMsg)) {
            errorMsg = "";
        }
        if (StringUtils.isBlank(errorStackTrace)) {
            errorStackTrace = "";
        }
        taskRepository.fail(task.getTaskId(), errorMsg, errorStackTrace);
        rpcFeedbackJobFail();
        // 终止其它执行中的task
    }

}
