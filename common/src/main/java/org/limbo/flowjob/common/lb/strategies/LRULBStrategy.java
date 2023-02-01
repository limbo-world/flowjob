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

package org.limbo.flowjob.common.lb.strategies;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.common.lb.AbstractLBStrategy;
import org.limbo.flowjob.common.lb.Invocation;
import org.limbo.flowjob.common.lb.LBServer;
import org.limbo.flowjob.common.lb.LBServerStatistics;
import org.limbo.flowjob.common.lb.LBServerStatisticsProvider;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Least Recently Used，选择最长时间没有被使用的 LBServer。
 *
 * @author Brozen
 * @since 2022-09-02
 */
@Slf4j
public class LRULBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    /**
     * 用于查询服务统计数据
     */
    private LBServerStatisticsProvider statisticsProvider;

    /**
     * 统计使用次数时，查询多久的统计数据
     */
    private Duration interval = Duration.ofMinutes(10);

    public LRULBStrategy(LBServerStatisticsProvider statisticsProvider) {
        this.statisticsProvider = Objects.requireNonNull(statisticsProvider);
    }

    /**
     * @param statisticsProvider 不可为 null
     */
    public void setStatisticsProvider(LBServerStatisticsProvider statisticsProvider) {
        this.statisticsProvider = Objects.requireNonNull(statisticsProvider);
    }


    /**
     * @param interval 不可为空，不可为 0 或负值。
     */
    public void setInterval(Duration interval) {
        if (interval.toMillis() <= 0) {
            this.interval = Duration.ofMinutes(10);
        } else {
            this.interval = interval;
        }
    }


    /**
     * {@inheritDoc}
     * @param servers 被负载的服务列表，可以保证非空。
     * @param invocation 本次调用的上下文信息
     * @return
     */
    @Override
    protected Optional<S> doSelect(List<S> servers, Invocation invocation) {
        Set<String> serverIds = servers.stream()
                .map(LBServer::getServerId)
                .collect(Collectors.toSet());

        List<LBServerStatistics> statistics = statisticsProvider.getStatistics(serverIds, this.interval);
        if (CollectionUtils.isEmpty(statistics)) {
            return Optional.empty();
        }

        return statistics.stream()
                .min(Comparator.comparing(LBServerStatistics::getLatestAccessAt))
                .flatMap(s -> servers.stream()
                        .filter(server -> StringUtils.equals(server.getServerId(), s.getServerId()))
                        .findFirst()
                );
    }

}
