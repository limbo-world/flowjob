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

import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.concurrent.Queues;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brozen
 * @since 2021-05-18
 */
public abstract class AsyncJobExecutorService implements JobExecutorService {

    /**
     * 记录此JobExecutor上执行的Job对应的上下文事件源。一个作业可能被多次调度，因此可能通过此事件源发射多此JobContext
     */
    private Map<String, UnicastProcessor<JobContext>> jobContextEventSources;

    public AsyncJobExecutorService() {
        this.jobContextEventSources = new ConcurrentHashMap<>();
    }

    /**
     * 获取作业分发时的上下文事件源
     * @param job 作业
     * @return 作业上下文事件源
     */
    protected UnicastProcessor<JobContext> getJobContextEventSource(Job job) {
        return jobContextEventSources.computeIfAbsent(job.getId(),
                jobId -> UnicastProcessor.create(Queues.<JobContext>get(10).get()));
    }

    /**
     * 移除作业分发时的上下文事件源，并在此事件源上发射complete事件。
     * @param job 作业
     * @return
     */
    protected UnicastProcessor<JobContext> removeJobContextEventSource(Job job) {
        UnicastProcessor<JobContext> eventSource = jobContextEventSources.remove(job.getId());
        if (eventSource != null) {
            eventSource.onComplete();
        }
        return eventSource;
    }

}
