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

package org.limbo.flowjob.broker.core.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.meta.info.JobInfo;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelectInvocation;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelector;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.core.worker.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.worker.dispatch.WorkerFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/12/29
 */
@Slf4j
public class WorkerDomainService {

    private WorkerRegistry workerRegistry;

    private WorkerSelectorFactory workerSelectorFactory;

    private WorkerStatisticsRepository workerStatisticsRepository;

    public WorkerDomainService(WorkerRegistry workerRegistry,
                               WorkerSelectorFactory workerSelectorFactory,
                               WorkerStatisticsRepository workerStatisticsRepository) {
        this.workerRegistry = workerRegistry;
        this.workerSelectorFactory = workerSelectorFactory;
        this.workerStatisticsRepository = workerStatisticsRepository;
    }

    public List<Worker> filterJobWorkers(JobInstance jobInstance, boolean filterExecutor, boolean filterTag, boolean filterResource, boolean lbSelect) {
        JobInfo jobInfo = jobInstance.getJobInfo();

        List<Worker> workers = workerRegistry.all().stream()
                .filter(Worker::isEnabled)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(workers)) {
            return Collections.emptyList();
        }

        DispatchOption dispatchOption = jobInfo.getDispatchOption();
        if (dispatchOption == null) {
            log.warn("Job has none dispatchOption id={}", jobInstance.getId());
            return Collections.emptyList();
        }

        // 过滤
        WorkerFilter workerFilter = new WorkerFilter(jobInfo.getExecutorName(), dispatchOption.getTagFilters(), workers);
        if (filterExecutor) {
            workerFilter.filterExecutor();
        }
        if (filterTag) {
            workerFilter.filterTags();
        }
        if (filterResource) {
            workerFilter.filterResources(dispatchOption.getCpuRequirement(), dispatchOption.getRamRequirement());
        }

        if (lbSelect) {
            WorkerSelectInvocation invocation = new WorkerSelectInvocation(jobInfo.getExecutorName(), jobInstance.getAttributes());
            WorkerSelector workerSelector = workerSelectorFactory.newSelector(jobInfo.getDispatchOption().getLoadBalanceType());
            Worker select = workerSelector.select(invocation, workerFilter.get());
            if (select == null) {
                return Collections.emptyList();
            } else {
                workerStatisticsRepository.recordDispatched(select);
                return Collections.singletonList(select);
            }
        } else {
            for (Worker worker : workerFilter.get()) {
                workerStatisticsRepository.recordDispatched(worker);
            }
            return workerFilter.get();
        }
    }
}
