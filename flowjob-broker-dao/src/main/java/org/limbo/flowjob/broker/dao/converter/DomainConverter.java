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

package org.limbo.flowjob.broker.dao.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.constants.AgentStatus;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * 基础信息转换 静态方法
 *
 * @author Devil
 * @since 2022/8/11
 */
@Slf4j
public class DomainConverter {

    public static ScheduleOption toScheduleOption(PlanInfoEntity entity) {
        return new ScheduleOption(
                ScheduleType.parse(entity.getScheduleType()),
                entity.getScheduleStartAt(),
                entity.getScheduleEndAt(),
                Duration.ofMillis(entity.getScheduleDelay()),
                Duration.ofMillis(entity.getScheduleInterval()),
                entity.getScheduleCron(),
                entity.getScheduleCronType()
        );
    }

    /**
     * @param dag 节点关系
     * @return job dag
     */
    public static DAG<WorkflowJobInfo> toJobDag(String dag) {
        List<WorkflowJobInfo> jobInfos = JacksonUtils.parseObject(dag, new TypeReference<List<WorkflowJobInfo>>() {
        });
        return new DAG<>(jobInfos);
    }

    public static JobInstanceEntity toJobInstanceEntity(JobInstance jobInstance) {
        WorkflowJobInfo jobInfo = jobInstance.getJobInfo();
        JobInstanceEntity entity = new JobInstanceEntity();
        entity.setJobId(jobInfo.getId());
        entity.setJobInstanceId(jobInstance.getId());
        entity.setAgentId(jobInstance.getAgentId());
        entity.setBrokerUrl(jobInstance.getBrokerUrl() == null ? "" : jobInstance.getBrokerUrl().toString());
        entity.setRetryTimes(jobInstance.getRetryTimes());
        entity.setInstanceId(jobInstance.getInstanceId());
        entity.setInstanceType(jobInstance.getInstanceType().type);
        entity.setStatus(jobInstance.getStatus().status);
        entity.setTriggerAt(jobInstance.getTriggerAt());
        entity.setStartAt(jobInstance.getStartAt());
        entity.setEndAt(jobInstance.getEndAt());
        entity.setLastReportAt(TimeUtils.currentLocalDateTime());
        return entity;
    }

    public static ScheduleAgent toAgent(AgentEntity entity) {
        return ScheduleAgent.builder()
                .id(entity.getAgentId())
                .status(AgentStatus.parse(entity.getStatus()))
                .rpcBaseUrl(url(entity))
                .availableQueueLimit(entity.getAvailableQueueLimit())
                .lastHeartbeatAt(entity.getLastHeartbeatAt())
                .enabled(entity.isEnabled())
                .build();
    }

    public static URL url(AgentEntity entity) {
        try {
            return new URL(entity.getProtocol(), entity.getHost(), entity.getPort(), "");
        } catch (Exception e) {
            throw new IllegalStateException("parse agent rpc info error", e);
        }
    }

    /**
     * 解析 broker 通信 URL
     */
    public static URL brokerUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new IllegalStateException("parse broker rpc info error", e);
        }
    }

}
