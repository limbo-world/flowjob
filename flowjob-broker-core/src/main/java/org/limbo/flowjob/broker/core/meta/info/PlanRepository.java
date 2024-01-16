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

package org.limbo.flowjob.broker.core.meta.info;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/5/8
 */
public interface PlanRepository {

    Plan get(String id);

    Plan lockAndGet(String id);

    Plan getByVersion(String id, String version);

    List<Plan> loadUpdatedPlans(URL brokerUrl, LocalDateTime updatedAt);

    /**
     * 获取不属于broker列表中broker管理的plan
     *
     * @param brokerUrls broker地址列表
     * @return planId, brokerUrl
     */
    Map<String, URL> findNotInBrokers(List<URL> brokerUrls, int limit);

    /**
     * 更新绑定的broker
     *
     * @param id           当前plan
     * @param oldBrokerUrl 旧的broker
     * @param newBrokerUrl 新的broker
     * @return 是否成功
     */
    boolean updateBroker(String id, URL oldBrokerUrl, URL newBrokerUrl);

}
