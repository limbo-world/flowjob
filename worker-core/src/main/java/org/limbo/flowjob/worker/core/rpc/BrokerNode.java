/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.core.rpc;

import org.limbo.flowjob.common.lb.LBServer;

import java.net.URL;

/**
 * @author Brozen
 * @since 2022-08-31
 */
public class BrokerNode implements LBServer {

    /**
     * broker 节点访问的 URL
     */
    private final URL baseUrl;

    public BrokerNode(URL baseUrl) {
        this.baseUrl = baseUrl;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getServerId() {
        return baseUrl.toString();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public boolean isAlive() {
        // TODO 这里先不考虑 broker 的熔断、失活检测
        return true;
    }

    @Override
    public URL getUrl() {
        return baseUrl;
    }

}
