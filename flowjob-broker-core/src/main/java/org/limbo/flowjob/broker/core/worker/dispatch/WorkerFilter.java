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

package org.limbo.flowjob.broker.core.worker.dispatch;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于配置过滤出合适的worker
 */
public class WorkerFilter {

    private String executorName;
    private List<TagFilterOption> tagFilters;
    private List<Worker> workers;

    public WorkerFilter(String executorName, List<TagFilterOption> tagFilters, List<Worker> workers) {
        this.executorName = executorName;
        this.tagFilters = tagFilters;
        this.workers = CollectionUtils.isEmpty(workers) ? Collections.emptyList() : workers;
    }

    /**
     * 基于执行器选择
     */
    public WorkerFilter filterExecutor() {
        this.workers = workers.stream()
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
        return this;
    }


    /**
     * 基于标签选择
     */
    public WorkerFilter filterTags() {
        if (CollectionUtils.isEmpty(tagFilters)) {
            return this;
        }
        for (TagFilterOption tagFilter : tagFilters) {
            this.workers = workers.stream().filter(tagFilter.asPredicate()).collect(Collectors.toList());
        }
        return this;
    }

    /**
     * 基于资源过滤
     */
    public WorkerFilter filterResources() {
        this.workers = workers.stream().filter(worker -> {
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
        return this;
    }

    /**
     * 获取worker
     */
    public List<Worker> get() {
        return workers;
    }

}
