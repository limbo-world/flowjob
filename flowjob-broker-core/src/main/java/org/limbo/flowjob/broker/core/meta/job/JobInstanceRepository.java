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

package org.limbo.flowjob.broker.core.meta.job;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/5/8
 */
public interface JobInstanceRepository {

    JobInstance get(String id);

    void save(JobInstance jobInstance);

    void saveAll(List<JobInstance> jobInstances);

    boolean executing(String jobInstanceId, String agentId, LocalDateTime startAt);

    boolean success(String jobInstanceId, LocalDateTime endAt, String context);

    boolean fail(String jobInstanceId, Integer oldStatus, LocalDateTime startAt, LocalDateTime endAt, String errorMsg);

    boolean report(String jobInstanceId, LocalDateTime lastReportAt);

    JobInstance getLatest(String planInstanceId, String jobId);

    List<JobInstance> findByExecuteCheck(URL brokerUrl, LocalDateTime lastReportAtStart, LocalDateTime lastReportAtEnd, String startId, Integer limit);

    List<JobInstance> findInSchedule(URL brokerUrl, LocalDateTime lastReportAt, LocalDateTime triggerAt, String startId, Integer limit);

    /**
     * 获取不属于broker列表中broker管理的 JobInstance
     *
     * @param brokerUrls broker地址列表
     * @return jobInstanceId, brokerUrl
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
