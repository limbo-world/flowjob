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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Brozen
 * @since 2022-09-02
 */
@Slf4j
public class AppointLBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    /**
     * 通过节点RPC通信URL指定负载均衡节点
     */
    public static final String PARAM_BY_URL = "appoint.byUrl";

    /**
     * 通过节点 ID 指定负载均衡节点
     */
    public static final String PARAM_BY_SERVER_ID = "appoint.byServerId";


    /**
     * 从调用参数中解析指定节点的类型。
     */
    private boolean filter(S server, Invocation invocation) {
        if (server == null || server.getUrl() == null) {
            return false;
        }
        Map<String, String> params = invocation.getLBParameters();
        String byServerId = params.get(PARAM_BY_SERVER_ID);
        if (StringUtils.isNotBlank(byServerId) && StringUtils.equals(server.getServerId(), byServerId)) {
            return true;
        }

        String byUrl = params.get(PARAM_BY_URL);
        if (StringUtils.isNotBlank(byUrl) && server.getUrl().toString().equals(byUrl)) {
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
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
                .filter(server -> filter(server, invocation))
                .findFirst();
    }

}
