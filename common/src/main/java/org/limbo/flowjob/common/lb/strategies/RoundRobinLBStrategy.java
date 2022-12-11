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
import org.limbo.flowjob.common.lb.AbstractLBStrategy;
import org.limbo.flowjob.common.lb.LBServer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Brozen
 * @since 2022-09-02
 */
// todo 这个计算方式有问题
@Slf4j
public class RoundRobinLBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    /**
     * 记录轮询索引
     */
    private final AtomicInteger index;


    public RoundRobinLBStrategy() {
        this.index = new AtomicInteger();
    }


    /**
     * 计算下一个服务索引
     * @param serverCount 服务总数
     */
    protected int nextIndex(int serverCount) {
        while (true) {
            int idx = this.index.get();
            int nextIdx = (idx + 1) % serverCount;
            if (this.index.compareAndSet(idx, nextIdx)) {
                return nextIdx;
            }
        }
    }

    @Override
    protected Optional<S> selectNonEmpty(List<S> servers) {
        // 这里不只取 aliveServers 遍历，防止遍历过程中，servers 中的服务从不可用变为可用
        // 找到轮询的下一个服务
        int idx = nextIndex(servers.size());
        S selected = servers.get(idx);

        // 服务不存在，重新尝试获取
        if (selected == null || !selected.isAlive()) {
            return Optional.empty();
        }

        return Optional.of(selected);
    }
}
