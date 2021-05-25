/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.job.context;

import org.limbo.flowjob.tracker.core.exceptions.JobContextException;
import org.limbo.flowjob.tracker.core.job.attribute.JobAttributes;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface JobContext {

    /**
     * 获取作业ID。
     * @return 当前执行中的作业ID。
     */
    String getJobId();

    /**
     * 获取当前作业上下文ID。一个作业可能在调度中，有两次同时在执行，因此可能会产生两个context，需要用contextId做区分。
     * @return 当前作业上下文ID
     */
    String getContextId();

    /**
     * 获取当前作业上下文状态。
     * @return 当前上下文状态
     */
    Status getStatus();

    /**
     * 获取作业属性。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     * @return {@link JobAttributes}
     */
    JobAttributes getJobAttributes();

    /**
     * 获取执行此作业的worker id
     * @return 执行此作业的worker id
     */
    String getWorkerId();

    /**
     * 获取此上下文的创建时间
     * @return 此上下文的创建时间
     */
    LocalDateTime getCreatedAt();

    /**
     * 获取此上下文的更新时间
     * @return 此上下文的更新时间
     */
    LocalDateTime getUpdatedAt();

    /**
     * 在指定worker上启动此作业上下文，将作业上下文发送给worker。
     * 只有{@link JobContextStatus#INIT}和{@link JobContextStatus#FAILED}状态的上下文可被开启。
     * @param worker 会将此上下文分发去执行的worker
     */
    void startupContext(Worker worker) throws JobContextException;

    /**
     * worker确认接收此作业上下文，表示开始执行作业
     * @param worker 确认接收此上下文的worker
     */
    void acceptContext(Worker worker) throws JobContextException;

    /**
     * worker拒绝接收此作业上下文，作业不会开始执行
     * @param worker 拒绝接收此上下文的worker
     */
    void refuseContext(Worker worker) throws JobContextException;

    /**
     * 关闭上下文，只有绑定该上下文的作业执行完成后，才会调用此方法。
     */
    void closeContext() throws JobContextException;

    /**
     * 上下文被worker拒绝时的回调监听。
     * @return 上下文被worker拒绝执行时触发
     */
    Mono<JobContext> onContextRefused();

    /**
     * 上下文被worker接收时的回调监听。
     * @return 上下文被worker接收时触发
     */
    Mono<JobContext> onContextAccepted();

    /**
     * 上下文被关闭时的回调监听。
     * @return 上下文被关闭时触发
     */
    Mono<JobContext> onContextClosed();


    /**
     * 上下文状态
     */
    interface Status {

        /**
         * 状态码
         */
        int getStatus();

        /**
         * 状态描述
         */
        String getDesc();
    }

}
