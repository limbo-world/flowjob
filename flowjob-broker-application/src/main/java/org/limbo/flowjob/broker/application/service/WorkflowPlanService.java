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

package org.limbo.flowjob.broker.application.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.WorkflowPlanUpdateParam;
import org.limbo.flowjob.broker.application.converter.WorkflowPlanConverter;
import org.limbo.flowjob.broker.application.converter.WorkflowPlanParamConverter;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Brozen
 * @since 2023-08-11
 */
@Slf4j
@Service
public class WorkflowPlanService {

    @Setter(onMethod_ = @Inject)
    private PlanAppService planAppService;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkflowPlanConverter converter;


    /**
     * 新增 DAG 任务
     */
    @Transactional
    public String add(PlanParam.WorkflowPlanParam param) {
        Verifies.notEmpty(param.getWorkflow(), "Workflow can't be empty with " + InstanceType.WORKFLOW.name() + " Type");
        DAG<WorkflowJobInfo> workflow = WorkflowPlanParamConverter.createDAG(param.getWorkflow());

        String jobInfo = workflow.json();
        Verifies.notBlank(jobInfo, "Dag Node verify fail!");

        return planAppService.save(null, InstanceType.WORKFLOW, param, jobInfo);
    }


    /**
     * 更新 DAG 任务
     */
    @Transactional
    public String update(WorkflowPlanUpdateParam param) {
        Verifies.notEmpty(param.getWorkflow());
        DAG<WorkflowJobInfo> workflow = WorkflowPlanParamConverter.createDAG(param.getWorkflow());

        String jobInfo = workflow.json();
        Verifies.notBlank(jobInfo);

        return planAppService.save(param.getPlanId(), InstanceType.WORKFLOW, param, jobInfo);
    }


    /**
     * 获取 DAG 任务详情
     * @param planId 任务 ID
     */
    public PlanInfoDTO.WorkflowPlanInfoDTO getWorkflowPlan(String planId) {
        // 查询任务信息并校验
        PlanInfoEntity planInfo = planEntityRepo.findById(planId)
                .flatMap(plan -> planInfoEntityRepo.findById(plan.getCurrentVersion()))
                .orElseThrow(() -> new VerifyException(String.format("Cannot find Plan %s", planId)));

        return converter.toWorkflowPlanDTO(planInfo);
    }

}
