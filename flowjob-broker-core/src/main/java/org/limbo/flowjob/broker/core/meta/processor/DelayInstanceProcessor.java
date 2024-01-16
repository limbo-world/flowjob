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

package org.limbo.flowjob.broker.core.meta.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.IDGenerator;
import org.limbo.flowjob.broker.core.meta.IDType;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.instance.DelayInstance;
import org.limbo.flowjob.broker.core.meta.instance.DelayInstanceRepository;
import org.limbo.flowjob.broker.core.meta.instance.Instance;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceFactory;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.task.JobInstanceTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.service.TransactionService;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理整体调度的逻辑
 *
 * @author Devil
 * @since 2023/12/7
 */
@Slf4j
public class DelayInstanceProcessor extends InstanceProcessor {

    private final DelayInstanceRepository delayInstanceRepository;

    public DelayInstanceProcessor(MetaTaskScheduler metaTaskScheduler,
                                  IDGenerator idGenerator,
                                  NodeManger nodeManger,
                                  AgentRegistry agentRegistry,
                                  TransactionService transactionService,
                                  DelayInstanceRepository delayInstanceRepository,
                                  JobInstanceRepository jobInstanceRepository) {
        super(agentRegistry, nodeManger, idGenerator, metaTaskScheduler, transactionService, jobInstanceRepository);
        this.delayInstanceRepository = delayInstanceRepository;
    }

    public void schedule(DelayInstance instance) {
        ScheduleContext scheduleContext = new ScheduleContext();
        transactionService.transactional(() -> {
            DelayInstance existInstance = delayInstanceRepository.get(instance.getBizType(), instance.getBizId());
            Verifies.isNull(existInstance, MessageFormat.format("create instance bizType:{0} bizId:{1} but is already exist", instance.getBizType(), instance.getBizId()));

            delayInstanceRepository.save(instance);

            // 获取头部节点
            List<JobInstance> jobInstances = new ArrayList<>();
            for (WorkflowJobInfo jobInfo : instance.getDag().origins()) {
                if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                    String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
                    Node elect = nodeManger.elect(jobInstanceId);
                    jobInstances.add(JobInstanceFactory.create(jobInstanceId, instance.getId(), instance.getType(), elect.getUrl(), null, new Attributes(), jobInfo, instance.getTriggerAt()));
                }
            }
            jobInstanceRepository.saveAll(jobInstances);
            scheduleContext.setWaitScheduleJobs(jobInstances);
            return null;
        });

        asyncSchedule(scheduleContext);
    }


    @Override
    protected Instance lockAndGet(String instanceId) {
        return delayInstanceRepository.lockAndGet(instanceId);
    }

    @Override
    protected void asyncSchedule(ScheduleContext scheduleContext) {
        if (scheduleContext == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(scheduleContext.getWaitScheduleJobs())) {
            for (JobInstance jobInstance : scheduleContext.getWaitScheduleJobs()) {
                JobInstanceTask metaTask = new JobInstanceTask(jobInstance, agentRegistry);
                metaTaskScheduler.schedule(metaTask);
            }
        }
    }

    @Override
    protected boolean instanceExecuting(String instanceId) {
        return delayInstanceRepository.executing(instanceId, TimeUtils.currentLocalDateTime());
    }

    @Override
    protected void handlerInstanceComplete(String instanceId, boolean success, ScheduleContext scheduleContext) {
        DelayInstance instance = delayInstanceRepository.get(instanceId);
        Verifies.notNull(instance, MsgConstants.CANT_FIND_DELAY_INSTANCE + instanceId);
        if (success) {
            delayInstanceRepository.success(instanceId, TimeUtils.currentLocalDateTime());
        } else {
            LocalDateTime current = TimeUtils.currentLocalDateTime();
            LocalDateTime startAt = instance.getStartAt() == null ? current : instance.getStartAt();
            delayInstanceRepository.fail(instanceId, startAt, current);
        }
    }

}
