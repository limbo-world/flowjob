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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.application.schedule.ScheduleProxy;
import org.limbo.flowjob.broker.application.support.CommonThreadPool;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.common.constants.JobConstant;
import org.limbo.flowjob.common.utils.time.DateTimeUtils;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * job 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致job没有完成的原因
 * 1. agent服务真实下线
 * 2. agent服务假死
 * 3. agent完成job调用broker的接口失败
 */
@Slf4j
@Component
public class JobExecuteCheckTask extends FixDelayMetaTask {

    private final JobInstanceEntityRepo jobInstanceEntityRepo;

    private final BrokerSlotManager slotManager;

    private final Broker broker;

    private final NodeManger nodeManger;

    private final ScheduleProxy scheduleProxy;

    /**
     * 上次检测时间
     */
    private LocalDateTime lastCheckTime = DateTimeUtils.parse("2000-01-01 00:00:00", Formatters.YMD_HMS);

    public JobExecuteCheckTask(MetaTaskScheduler metaTaskScheduler,
                               JobInstanceEntityRepo jobInstanceEntityRepo,
                               BrokerSlotManager slotManager,
                               @Lazy Broker broker,
                               NodeManger nodeManger,
                               ScheduleProxy scheduleProxy) {
        super(Duration.ofSeconds(5), metaTaskScheduler);
        this.jobInstanceEntityRepo = jobInstanceEntityRepo;
        this.slotManager = slotManager;
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.scheduleProxy = scheduleProxy;
    }

    @Override
    protected void executeTask() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(broker.getName())) {
                return;
            }

            List<String> planIds = slotManager.planIds();
            if (CollectionUtils.isEmpty(planIds)) {
                return;
            }

            LocalDateTime curCheckTime = TimeUtils.currentLocalDateTime().plus(-JobConstant.JOB_REPORT_SECONDS + 5, ChronoUnit.SECONDS);

            Integer limit = 100;

            List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByExecuteCheck(planIds, JobStatus.EXECUTING.status, lastCheckTime, curCheckTime, limit);
            while (CollectionUtils.isNotEmpty(jobInstanceEntities)) {
                for (JobInstanceEntity instance : jobInstanceEntities) {
                    CommonThreadPool.IO.submit(() -> {
                        try {
                            JobFeedbackParam param = JobFeedbackParam.builder()
                                    .result(ExecuteResult.FAILED)
                                    .errorMsg(String.format("agent %s is offline", instance.getAgentId()))
                                    .build();
                            scheduleProxy.feedback(instance.getJobInstanceId(), param);
                        } catch (Exception e) {
                            log.error("[JobExecuteCheckTask] handler job fail with error jobInstanceId={}", instance.getJobInstanceId(), e);
                        }
                    });
                }
                jobInstanceEntities = jobInstanceEntityRepo.findByExecuteCheck(planIds, JobStatus.EXECUTING.status, lastCheckTime, curCheckTime, limit);
            }
            lastCheckTime = curCheckTime;
        } catch (Exception e) {
            log.error("{} execute fail", scheduleId(), e);
        }
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
