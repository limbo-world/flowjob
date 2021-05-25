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

package org.limbo.flowjob.tracker.core.job.dag;

import org.limbo.flowjob.tracker.core.job.context.JobContext;
import reactor.core.publisher.Flux;

/**
 * TODO
 *
 * @author Brozen
 * @since 2021-05-19
 */
public interface DAGJobContext extends JobContext {

    /**
     * 关闭子上下文，此上下文对应的DAG作业中的子作业执行完成后，会调用此方法。
     * @return 返回子上下文
     */
    JobContext closeChildContext(String childContextId);

    /**
     * 此上下文作业的子作业上下文被关闭时的回调监听。
     */
    Flux<JobContext> onChildContextClosed();

}
