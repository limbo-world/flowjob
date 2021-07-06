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

package org.limbo.flowjob.tracker.core.dispatcher.strategies;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public abstract class AbstractJobDispatcher implements JobDispatcher {

    /**
     * {@inheritDoc}
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @param callback 作业执行回调
     */
    @Override
    public void dispatch(JobContext context, Collection<Worker> workers, BiConsumer<JobContext, Worker> callback) {
        if (CollectionUtils.isEmpty(workers)) {
            throw new JobWorkerException(context.getJobId(), null, "No worker available!");
        }

        Worker worker = selectWorker(context, workers);
        callback.accept(context, worker);
    }

    /**
     * 选择一个worker进行作业下发
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @return 需要下发作业上下文的worker
     */
    protected abstract Worker selectWorker(JobContext context, Collection<Worker> workers);

}
