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

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author Brozen
 * @since 2022-09-02
 */
// todo 这个感觉应该和其他策略配合使用，如果指定节点找到一批，如何对这一批做负载
@Slf4j
public class AppointLBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    /**
     * 通过节点 ID 指定负载均衡节点
     * @param serverId 节点 ID
     * @param <S> 负载均衡节点类型，可实现 LBServer 接口
     */
    public static <S extends LBServer> AppointLBStrategy<S> byId(String serverId) {
        return new AppointLBStrategy<>((server, invocation) -> StringUtils.equals(server.getServerId(), serverId));
    }


    /**
     * 通过节点RPC通信URL指定负载均衡节点
     * @param url 节点RPC通信地址
     * @param <S> 负载均衡节点类型，可实现 LBServer 接口
     */
    public static <S extends LBServer> AppointLBStrategy<S> byUrl(URL url) {
        Objects.requireNonNull(url);
        return new AppointLBStrategy<>((server, invocation) -> url.equals(server.getUrl()));
    }


    /**
     * 判断节点是否符合指定条件
     */
    private final BiPredicate<S, Invocation> condition;


    private AppointLBStrategy(BiPredicate<S, Invocation> condition) {
        this.condition = Objects.requireNonNull(condition);
    }


    /**
     * {@inheritDoc}
     * @param servers
     * @param invocation
     * @return
     */
    @Override
    protected Optional<S> doSelect(List<S> servers, Invocation invocation) {
        if (CollectionUtils.isEmpty(servers)) {
            return Optional.empty();
        }

        return servers.stream()
                .filter(s -> this.condition.test(s, invocation))
                .findFirst();
    }

}
