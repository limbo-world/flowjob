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
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface JobContext extends JobContextDefinition {

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
