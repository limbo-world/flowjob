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

package org.limbo.flowjob.broker.core.domain.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.domain.DispatchOption;
import org.limbo.flowjob.broker.core.domain.ExecutorOption;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Slf4j
@Getter
@Setter
@ToString
public class Task implements Serializable {
    private static final long serialVersionUID = -9164373359695671417L;

    private String taskId;

    private String jobId;

    private String planVersion;

    private String jobInstanceId;

    /**
     * 状态
     */
    private TaskStatus status;

    private TaskType type;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * 上下文
     */
    private Attributes context;
    /**
     * 作业属性，不可变。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     */
    private Attributes attributes;
    /**
     * reduce作业属性
     */
    private List<Attributes> reduceAttributes;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    /**
     * 重试次数
     */
    @Setter(AccessLevel.NONE)
    private Integer retry = 3;

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private ExecutorOption executorOption;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient WorkerManager workerManager;

    /**
     * 将此任务下发给worker。
     *
     * @param workerSelector 会将此上下文分发去执行的worker
     */
    public void dispatch(WorkerSelector workerSelector) {
        if (getStatus() != TaskStatus.DISPATCHING) {
            throw new JobDispatchException(jobId, taskId, "Cannot startup context due to current status: " + status);
        }

        List<Worker> availableWorkers = workerManager.availableWorkers();
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return;
        }
        for (int i = 0; i < retry; i++) {
            try {
                Worker worker = workerSelector.select(this, availableWorkers);
                if (worker == null) {
                    return;
                }

                // 发送任务到worker，根据worker返回结果，更新状态
                TaskReceiveDTO result = worker.sendTask(this);
                boolean dispatched = result != null && result.getAccepted();
                if (dispatched) {

                    // 更新状态
                    setStatus(TaskStatus.EXECUTING);
                    setWorkerId(worker.getWorkerId());

                    return;
                }

                availableWorkers = availableWorkers.stream().filter(w -> !Objects.equals(w.getWorkerId(), worker.getWorkerId())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Task dispatch fail task={}", this, e);
            }
        }

        // 下发失败
        setStatus(TaskStatus.FAILED);
    }

}
