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

package org.limbo.flowjob.tracker.core.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;
import org.limbo.flowjob.tracker.core.storage.Storage;

import java.util.List;

/**
 *
 * 计划执行器
 * 生成 plan 以及 job 的实例 并保存
 * @author Brozen
 * @since 2021-07-13
 */
public class PlanExecutor implements Executor<Plan> {

//    private final Storage storage;
//
//    public PlanExecutor(Storage storage) {
//        this.storage = storage;
//    }

    private final EventPublisher<Event<?>> eventEventPublisher;

    public PlanExecutor(EventPublisher<Event<?>> eventEventPublisher) {
        this.eventEventPublisher = eventEventPublisher;
    }

    /**
     * 将数据存储，交由下游，劲量不阻塞时间轮线程
     * @param plan 计划
     */
    @Override
    public void execute(Plan plan) {
        // 校验能否下发
        List<Job> jobs = plan.getDag().getEarliestJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }

        // todo 如果 plan 需要持久化，那么持久化一个 PlanRecord 那么如果出现主从切换，从节点会获取到这个数据并执行下发 如果plan不需要持久化 那么plan存在内存，如果主节点挂了这次执行可能就会丢失
//        storage.store(plan);
        eventEventPublisher.publish(new Event<>(plan));
    }


}
