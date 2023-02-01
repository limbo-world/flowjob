/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.dispatcher;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.dispatch.TagFilterOption;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.RPCInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class FilteringWorkerSelector implements WorkerSelector {

    private final LBStrategy<Worker> strategy;

    public FilteringWorkerSelector(LBStrategy<Worker> strategy) {
        this.strategy = strategy;
    }

    /**
     * {@inheritDoc}
     * @param args worker 选择参数
     * @param workers 待下发上下文可用的worker
     * @return
     */
    @Override
    public Worker select(WorkerSelectArgument args, List<Worker> workers) {
        if (CollectionUtils.isEmpty(workers)) {
            return null;
        }

        // 过滤 Worker
        List<Worker> availableWorkers = filterWorkers(args, workers);
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return null;
        }

        // 从过滤出的 worker 中，选择合适的
        return doSelect(args, availableWorkers);
    }


    /**
     * 过滤 Worker，从入参 worker 列表中选择合适的 Worker 作为 LB 策略的候选项。
     * 目前过滤条件有：
     * 1. executor 过滤
     * 2. tag 过滤
     */
    protected List<Worker> filterWorkers(WorkerSelectArgument args, List<Worker> workers) {
        DispatchOption dispatchOption = args.getDispatchOption();
        String executorName = args.getExecutorName();
        List<Worker> availableWorkers = new ArrayList<>();

        // 过滤出有指定 executor 的 Worker
        WORKER:
        for (Worker worker : workers) {
            List<WorkerExecutor> executors = worker.getExecutors();
            if (CollectionUtils.isEmpty(executors)) {
                continue;
            }
            // 判断是否有对应的执行器
            for (WorkerExecutor executor : executors) {
                if (executor.getName().equals(executorName)) {
                    availableWorkers.add(worker);
                    continue WORKER;
                }
            }
        }

        // TODO ??? 根据 CPU、内存剩余资源过滤

        // 根据标签过滤
        TagFilterOption tagFilter = dispatchOption.getTagFilter();
        if (tagFilter != null) {
            availableWorkers = availableWorkers.stream()
                    .filter(tagFilter.asPredicate())
                    .collect(Collectors.toList());
        }

        return availableWorkers;
    }


    /**
     * 执行 Worker 选择逻辑，这里默认使用负载均衡策略来代理选择逻辑。
     * PS：单独抽取一个方法，方便扩展。
     *
     * @param args 执行器名称
     * @param workers 待下发上下文可用的worker
     */
    protected Worker doSelect(WorkerSelectArgument args, List<Worker> workers) {
        RPCInvocation lbInvocation = RPCInvocation.builder()
                .path(args.getExecutorName())
                .lbParameters(args.getAttributes())
                .build();
        return strategy.select(workers, lbInvocation).orElse(null);
    }

}
