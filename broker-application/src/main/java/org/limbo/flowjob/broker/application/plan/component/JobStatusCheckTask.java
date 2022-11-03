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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixIntervalMetaTask;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.support.SlotManager;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需要将调度中的 job 进行下发
 */
public class JobStatusCheckTask extends FixIntervalMetaTask {

    @Setter(onMethod_ = @Inject)
    private JobScheduler scheduler;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private BrokerConfig config;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    public JobStatusCheckTask(Duration interval) {
        super("Meta[JobStatusCheckTask]", interval);
    }


    /**
     * 由于job也可能是在内存中等待下发，由于宕机等原因可能导致内存中job为调度中 但是已经没法下发了
     * 查询超时未下发的job
     */
    @Override
    protected void executeTask() {
        List<Integer> slots = SlotManager.slots(nodeManger.allAlive(), config.getHost(), config.getPort());
        if (CollectionUtils.isEmpty(slots)) {
            return;
        }
        List<PlanSlotEntity> slotEntities = planSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return;
        }
        Map<Long, Plan> planMap = new HashMap<>();
        for (PlanSlotEntity slotEntity : slotEntities) {
            Long planId = slotEntity.getPlanId();
            if (planMap.containsKey(planId)) {
                continue;
            }
            Plan plan = planRepository.get(planId.toString());
            planMap.put(planId, plan);
        }

        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanInstanceIdInAndStatusAndTriggerAtLessThan(
                planMap.keySet(),
                JobStatus.SCHEDULING.status,
                TimeUtils.currentLocalDateTime().plusSeconds(10)
        );

        if (CollectionUtils.isEmpty(jobInstanceEntities)) {
            return;
        }

        List<JobInstance> instances = new ArrayList<>();
        for (JobInstanceEntity jobInstanceEntity : jobInstanceEntities) {
            Plan plan = planMap.get(jobInstanceEntity.getPlanId());

            instances.add(DomainConverter.toJobInstance(jobInstanceEntity, plan.getInfo().getDag()));
        }


        for (JobInstance instance : instances) {
            scheduler.schedule(instance);
        }
    }

}
