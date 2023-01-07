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
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.core.schedule.strategy.IScheduleStrategy;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
public class Task implements MetaTask, Serializable {
    private static final long serialVersionUID = -9164373359695671417L;

    private String taskId;

    private String jobInstanceId;

    private String jobId;

    private String planId;

    private String planVersion;

    /**
     * 类型
     */
    private TaskType taskType;

    /**
     * 状态
     */
    private TaskStatus status;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * 全局上下文
     */
    private Attributes context;

    /**
     * 对应job配置的属性
     */
    private Attributes attributes;

    /**
     * 期望的触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    /**
     * 下发参数
     */
    private DispatchOption dispatchOption;

    /**
     * 执行器参数
     */
    private String executorName;

    /**
     * map属性
     */
    private Attributes mapAttributes;

    /**
     * reduce属性
     */
    private List<Attributes> reduceAttributes;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private IScheduleStrategy iScheduleStrategy;

    @Override
    public void execute() {
        iScheduleStrategy.schedule(this);
    }

    public void success(Map<String, Object> resultAttributes) {
        iScheduleStrategy.handleTaskSuccess(this, resultAttributes);
    }

    public void fail(String errorMsg, String errorStackTrace) {
        iScheduleStrategy.handleTaskFail(this, errorMsg, errorStackTrace);
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.TASK;
    }

    @Override
    public String getMetaId() {
        return taskId;
    }

    @Override
    public LocalDateTime scheduleAt() {
        return triggerAt;
    }

}
