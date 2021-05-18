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

package org.limbo.flowjob.tracker.core.executor;

import org.limbo.flowjob.tracker.core.Job;
import org.limbo.flowjob.tracker.core.JobContext;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.concurrent.Queues;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brozen
 * @since 2021-05-18
 */
public abstract class AsyncJobExecutor implements JobExecutor {

    /**
     * 记录此JobExecutor上执行的Job对应的上下文事件源。
     */
    private Map<String, UnicastProcessor<JobContext>> jobContextProcessors;

    public AsyncJobExecutor() {
        this.jobContextProcessors = new ConcurrentHashMap<>();
    }

    /**
     *
     * @param job
     * @return
     */
    protected UnicastProcessor<JobContext> getJobContextSubscriber(Job job) {
        return jobContextProcessors.computeIfAbsent(job.id(),
                jobId -> UnicastProcessor.create(Queues.<JobContext>get(10).get()));
    }

}
