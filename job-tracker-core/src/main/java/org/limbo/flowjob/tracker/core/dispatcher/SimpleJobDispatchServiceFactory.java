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

import org.limbo.flowjob.tracker.core.dispatcher.strategies.DefaultJobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class SimpleJobDispatchServiceFactory implements JobDispatchServiceFactory {

    private PlanRepository planRepository;

    public SimpleJobDispatchServiceFactory(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * {@inheritDoc}
     * @param context 作业上下文
     * @return
     */
    @Override
    public JobDispatchService newDispatchService(JobContext context) {
        return new SimpleJobDispatchService(new DefaultJobDispatcherFactory(planRepository));
    }

}
