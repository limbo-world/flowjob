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

package org.limbo.flowjob.broker.core.dispatch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.common.lb.LBServerStatisticsProvider;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.rpc.RPCInvocation;
import org.limbo.flowjob.common.lb.strategies.AppointLBStrategy;
import org.limbo.flowjob.common.lb.strategies.ConsistentHashLBStrategy;
import org.limbo.flowjob.common.lb.strategies.LFULBStrategy;
import org.limbo.flowjob.common.lb.strategies.LRULBStrategy;
import org.limbo.flowjob.common.lb.strategies.RandomLBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 作业分发配置，值对象
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
// 如果用下面自己写的构造函数的，字段要按顺序对应
@Builder(builderClassName = "Builder", toBuilder = true)
public class DispatchOption implements Serializable {

    private static final long serialVersionUID = 7742829408764721529L;
    /**
     * 分发方式
     */
    private LoadBalanceType loadBalanceType;

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     */
    private BigDecimal cpuRequirement;

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     */
    private BigDecimal ramRequirement;

    /**
     * tag 过滤器配置
     */
    private List<TagFilterOption> tagFilters;

    /**
     * @author Brozen
     * @since 2021-05-27
     */
    public static class FilteringWorkerSelector implements WorkerSelector {

        private final LBStrategy<Worker> strategy;

        public FilteringWorkerSelector(LBStrategy<Worker> strategy) {
            this.strategy = strategy;
        }

        /**
         * {@inheritDoc}
         *
         * @param args    worker 选择参数
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
            availableWorkers = filterResources(availableWorkers);
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
         * filter by worker queue/CPU/memory
         */
        protected List<Worker> filterResources(List<Worker> workers) {
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


        /**
         * 执行 Worker 选择逻辑，这里默认使用负载均衡策略来代理选择逻辑。
         * PS：单独抽取一个方法，方便扩展。
         *
         * @param args    执行器名称
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

    /**
     * @author Brozen
     * @since 2023-02-01
     */
    public interface WorkerSelectArgument {

        /**
         * 执行器名称
         */
        String getExecutorName();

        /**
         * 下发配置项
         */
        DispatchOption getDispatchOption();

        /**
         * 附加参数
         */
        Map<String, String> getAttributes();

    }

    /**
     * worker选择器，封装了作业分发时的worker选择规则{@link LoadBalanceType}：
     * <ul>
     *     <li>{@link LoadBalanceType#ROUND_ROBIN}</li>
     *     <li>{@link LoadBalanceType#RANDOM}</li>
     *     <li>{@link LoadBalanceType#APPOINT}</li>
     *     <li>{@link LoadBalanceType#LEAST_FREQUENTLY_USED}</li>
     *     <li>{@link LoadBalanceType#LEAST_RECENTLY_USED}</li>
     *     <li>{@link LoadBalanceType#CONSISTENT_HASH}</li>
     * </ul>
     *
     * @author Brozen
     * @since 2021-05-14
     */
    public static interface WorkerSelector {

        /**
         * 选择作业上下文应当下发给的worker。
         *
         * @param args    worker 选择参数
         * @param workers 待下发上下文可用的worker
         */
        Worker select(WorkerSelectArgument args, List<Worker> workers);

    }

    /**
     * {@link WorkerSelector} 工厂
     *
     * @author Brozen
     * @since 2021-05-18
     */
    public static class WorkerSelectorFactory {

        /**
         * 用于获取 LB 服务的统计信息，LRU、LFU 算法会用到。
         * 如果确认不使用 LRU、LFU 算法，可以不设置此属性
         */
        @Setter
        private LBServerStatisticsProvider lbServerStatisticsProvider = LBServerStatisticsProvider.EMPTY_PROVIDER;

        private final Map<LoadBalanceType, Supplier<WorkerSelector>> selectors = new EnumMap<>(LoadBalanceType.class);

        public WorkerSelectorFactory() {
            selectors.put(LoadBalanceType.ROUND_ROBIN, () -> new FilteringWorkerSelector(new RoundRobinLBStrategy<>()));
            selectors.put(LoadBalanceType.RANDOM, () -> new FilteringWorkerSelector(new RandomLBStrategy<>()));
            selectors.put(LoadBalanceType.LEAST_FREQUENTLY_USED, () -> new FilteringWorkerSelector(new LFULBStrategy<>(this.lbServerStatisticsProvider)));
            selectors.put(LoadBalanceType.LEAST_RECENTLY_USED, () -> new FilteringWorkerSelector(new LRULBStrategy<>(this.lbServerStatisticsProvider)));
            selectors.put(LoadBalanceType.APPOINT, () -> new FilteringWorkerSelector(new AppointLBStrategy<>()));
            selectors.put(LoadBalanceType.CONSISTENT_HASH, () -> new FilteringWorkerSelector(new ConsistentHashLBStrategy<>()));
        }

        /**
         * 根据作业的分发方式，创建一个分发器实例。委托给{@link LoadBalanceType}执行。
         *
         * @param loadBalanceType 分发类型
         * @return 作业分发器
         */
        public WorkerSelector newSelector(LoadBalanceType loadBalanceType) {
            return Optional.ofNullable(selectors.get(loadBalanceType))
                    .map(Supplier::get)
                    .orElseThrow(() -> new IllegalArgumentException(MsgConstants.UNKNOWN + " load balance type: " + loadBalanceType));
        }

    }
}
