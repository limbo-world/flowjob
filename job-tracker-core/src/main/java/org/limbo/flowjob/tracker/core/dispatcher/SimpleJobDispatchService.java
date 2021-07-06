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

package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobDispatchService implements JobDispatchService {

    /**
     * 用于生成JobDispatcher
     */
    private JobDispatcherFactory jobDispatcherFactory;

    public SimpleJobDispatchService(JobDispatcherFactory jobDispatcherFactory) {
        this.jobDispatcherFactory = jobDispatcherFactory;
    }

    /**
     * {@inheritDoc}
     * @param tracker tracker节点
     * @param context 待执行的作业上下文
     */
    @Override
    public void dispatch(JobTracker tracker, JobContext context) {

        JobDispatcher jobDispatcher = jobDispatcherFactory.newDispatcher(tracker, context);
        jobDispatcher.dispatch(context, tracker.availableWorkers(), JobContext::startupContext);

    }

}
