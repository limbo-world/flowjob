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

package org.limbo.flowjob.broker.application.task;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.application.schedule.ScheduleProxy;
import org.limbo.flowjob.broker.application.support.CommonThreadPool;
import org.limbo.flowjob.broker.core.agent.AgentRepository;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * job 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致job没有完成的原因
 * 1. agent服务真实下线
 * 2. agent服务假死
 * 3. agent完成job调用broker的接口失败
 */
@Component
public class JobExecuteCheckTask extends FixDelayMetaTask {

    private final AgentRepository agentRepository;

    private final JobInstanceEntityRepo jobInstanceEntityRepo;

    private final BrokerSlotManager slotManager;

    private final Broker broker;

    private final NodeManger nodeManger;

    private final ScheduleProxy scheduleProxy;

    public JobExecuteCheckTask(MetaTaskScheduler metaTaskScheduler,
                               JobInstanceEntityRepo jobInstanceEntityRepo,
                               BrokerSlotManager slotManager,
                               @Lazy Broker broker,
                               NodeManger nodeManger,
                               AgentRepository agentRepository,
                               ScheduleProxy scheduleProxy) {
        super(Duration.ofSeconds(5), metaTaskScheduler);
        this.jobInstanceEntityRepo = jobInstanceEntityRepo;
        this.slotManager = slotManager;
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.agentRepository = agentRepository;
        this.scheduleProxy = scheduleProxy;
    }

    @Override
    protected void executeTask() {
        // 判断自己是否存在 --- 可能由于心跳异常导致不存活
        if (!nodeManger.alive(broker.getName())) {
            return;
        }

        List<JobInstanceEntity> executingJobs = loadExecuting();
        if (CollectionUtils.isEmpty(executingJobs)) {
            return;
        }
        // 获取长时间为执行中的task 判断worker是否已经宕机
        for (JobInstanceEntity instance : executingJobs) {
            CommonThreadPool.IO.submit(() -> {
                ScheduleAgent agent = agentRepository.get(instance.getAgentId());
                if (agent == null || !agent.isAlive()) {
                    JobFeedbackParam param = JobFeedbackParam.builder()
                            .result(ExecuteResult.FAILED)
                            .errorMsg(String.format("agent %s is offline", instance.getAgentId()))
                            .build();
                    scheduleProxy.feedback(instance.getJobInstanceId(), param);
                }
            });
        }
    }

    /**
     * 加载执行中的
     */
    private List<JobInstanceEntity> loadExecuting() {
        List<String> planIds = slotManager.planIds();
        if (CollectionUtils.isEmpty(planIds)) {
            return Collections.emptyList();
        }
        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanIdInAndStatus(planIds, JobStatus.EXECUTING.status); // todo 性能优化
        if (CollectionUtils.isEmpty(jobInstanceEntities)) {
            return Collections.emptyList();
        }
        return jobInstanceEntities;
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.JOB_EXECUTE_CHECK;
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
