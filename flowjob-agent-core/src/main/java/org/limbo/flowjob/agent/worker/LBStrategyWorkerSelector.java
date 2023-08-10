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

package org.limbo.flowjob.agent.worker;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.lb.Invocation;
import org.limbo.flowjob.common.lb.LBStrategy;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class LBStrategyWorkerSelector implements WorkerSelector {

    private final LBStrategy<Worker> strategy;

    public LBStrategyWorkerSelector(LBStrategy<Worker> strategy) {
        this.strategy = strategy;
    }

    /**
     * {@inheritDoc}
     * 执行 Worker 选择逻辑，这里默认使用负载均衡策略来代理选择逻辑。
     * PS：单独抽取一个方法，方便扩展。
     *
     * @param invocation 选择内容
     * @param workers 待下发上下文可用的worker
     * @return
     */
    @Override
    public Worker select(Invocation invocation, List<Worker> workers) {
        if (CollectionUtils.isEmpty(workers)) {
            return null;
        }
        return strategy.select(workers, invocation).orElse(null);
    }

}
