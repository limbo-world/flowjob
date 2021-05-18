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

import io.netty.util.HashedWheelTimer;
import org.limbo.flowjob.tracker.core.Job;
import org.limbo.flowjob.tracker.core.JobContext;
import org.limbo.flowjob.tracker.core.exceptions.JobExecuteException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.util.concurrent.TimeUnit;

/**
 * 基于Netty时间轮算法的作业执行器。一个作业申请执行后，会计算下次执行的间隔，并注册到时间轮上。
 * 当时间轮触发作业执行时，将进入作业下发流程，并将生成的JobContext分发给下游。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public abstract class HashedWheelTimerJobExecutor extends AsyncJobExecutor {

    /**
     * 依赖netty的时间轮算法进行任务调度
     */
    private HashedWheelTimer timer;

    public HashedWheelTimerJobExecutor() {
        timer = new HashedWheelTimer(NamedThreadFactory.newInstance(this.getClass().getSimpleName() + "-timer-"));
    }

    /**
     * {@inheritDoc}<br/>
     *
     * 内部依赖时间轮对任务进行调度，execute的job会被注册到时间轮上。
     *
     * @param job 待执行的作业
     * @return
     * @throws JobExecuteException
     */
    @Override
    public Flux<JobContext> execute(Job job) {

        // 计算下次执行间隔
        long delayMillis = calculateNextTriggerDelay(job);

        UnicastProcessor<JobContext> processor = getJobContextSubscriber(job);
        if (delayMillis <= 0) {
            // 立即执行
            executeJobNow(job).subscribe(processor);
        } else {
            // 提交作业到时间轮，并异步触发
            timer.newTimeout(timeout -> executeJobNow(job).subscribe(processor), delayMillis, TimeUnit.MILLISECONDS);
        }

        return processor;
    }

    /**
     * {@inheritDoc}
     * @param job 待执行作业
     * @return
     */
    @Override
    public Mono<JobContext> executeJobNow(Job job) {
        // TODO
        return Mono.just(null);
    }

    /**
     * 计算作业下一次被触发时，距当前时刻还有多久。
     * @param job 作业
     * @return 作业下次触发距当前时刻的时间间隔的毫秒数
     */
    protected abstract long calculateNextTriggerDelay(Job job);



}
