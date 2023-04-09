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
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.dispatch.TagFilterOption;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.RPCInvocation;

import java.util.ArrayList;
import java.util.Collections;
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
        String executorName = args.getExecutorName();
        List<Worker> availableWorkers = filterExecutor(executorName, workers);
        DispatchOption dispatchOption = args.getDispatchOption();
        List<TagFilterOption> tagFilters = dispatchOption.getTagFilters();
        availableWorkers = filterTags(tagFilters, availableWorkers);
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return null;
        }

        // 从过滤出的 worker 中，选择合适的
        return doSelect(args, availableWorkers);
    }

    /**
     * filter by executor name
     */
    protected List<Worker> filterExecutor(String executorName, List<Worker> workers) {
        if (StringUtils.isBlank(executorName)) {
            return Collections.emptyList();
        }
        return workers.stream()
                .filter(worker -> {
                    List<WorkerExecutor> executors = worker.getExecutors();
                    if (CollectionUtils.isEmpty(executors)) {
                        return false;
                    }
                    // 判断是否有对应的执行器
                    for (WorkerExecutor executor : executors) {
                        if (executor.getName().equals(executorName)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * filter by tags
     */
    protected List<Worker> filterTags(List<TagFilterOption> tagFilters, List<Worker> workers) {
        if (CollectionUtils.isEmpty(tagFilters)) {
            return workers;
        }
        List<Worker> availableWorkers = new ArrayList<>(workers);
        for (TagFilterOption tagFilter : tagFilters) {
            availableWorkers = availableWorkers.stream().filter(tagFilter.asPredicate()).collect(Collectors.toList());
        }
        return availableWorkers;
    }

    /**
     * todo ??? filter by worker queue/CPU/memory
     */
    protected List<Worker> filterResources() {
        return null;
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
