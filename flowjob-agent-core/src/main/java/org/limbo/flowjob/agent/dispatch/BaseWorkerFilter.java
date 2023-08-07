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

package org.limbo.flowjob.agent.dispatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.worker.Worker;
import org.limbo.flowjob.agent.worker.WorkerAvailableResource;
import org.limbo.flowjob.agent.worker.WorkerExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BaseWorkerFilter implements WorkerFilter {

    @Override
    public List<Worker> filter(WorkerSelectArgument args, List<Worker> workers) {
        if (CollectionUtils.isEmpty(workers)) {
            return Collections.emptyList();
        }
        // 过滤 Worker
        String executorName = args.getExecutorName();
        List<Worker> availableWorkers = filterExecutor(executorName, workers);
        DispatchOption dispatchOption = args.getDispatchOption();
        List<TagFilterOption> tagFilters = dispatchOption.getTagFilters();
        availableWorkers = filterTags(tagFilters, availableWorkers);
        availableWorkers = filterResources(availableWorkers);
        return availableWorkers;
    }

    /**
     * filter by executor name
     */
    private List<Worker> filterExecutor(String executorName, List<Worker> workers) {
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
    private List<Worker> filterTags(List<TagFilterOption> tagFilters, List<Worker> workers) {
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
     * filter by worker queue/CPU/memory
     */
    private List<Worker> filterResources(List<Worker> workers) {
        if (CollectionUtils.isEmpty(workers)) {
            return Collections.emptyList();
        }
        return workers.stream().filter(worker -> {
            WorkerAvailableResource availableResource = worker.getMetric().getAvailableResource();
            if (availableResource.getAvailableQueueLimit() <= 0) {
                return false;
            }
            if (availableResource.getAvailableCpu() <= 0) {
                return false;
            }
            if (availableResource.getAvailableRam() <= 0) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

}
