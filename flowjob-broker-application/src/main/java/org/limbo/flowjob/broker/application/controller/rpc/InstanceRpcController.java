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

package org.limbo.flowjob.broker.application.controller.rpc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.broker.DelayInstanceCommitParam;
import org.limbo.flowjob.api.param.broker.PlanInstanceCommitParam;
import org.limbo.flowjob.api.param.broker.PlanInstanceJobScheduleParam;
import org.limbo.flowjob.broker.application.converter.JobParamConverter;
import org.limbo.flowjob.broker.core.meta.IDGenerator;
import org.limbo.flowjob.broker.core.meta.IDType;
import org.limbo.flowjob.broker.core.meta.info.Plan;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.instance.DelayInstance;
import org.limbo.flowjob.broker.core.meta.instance.InstanceFactory;
import org.limbo.flowjob.broker.core.meta.processor.DelayInstanceProcessor;
import org.limbo.flowjob.broker.core.meta.processor.PlanInstanceProcessor;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collections;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.*;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Tag(name = "plan remote rpc")
@RestController
public class InstanceRpcController {

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceProcessor planInstanceProcessor;

    @Setter(onMethod_ = @Inject)
    private DelayInstanceProcessor delayInstanceProcessor;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    /**
     * 手动触发对应 plan
     */
    @Operation(summary = "触发对应plan调度")
    @PostMapping(API_PLAN_INSTANCE_COMMIT)
    public ResponseDTO<String> schedulePlan(@RequestBody PlanInstanceCommitParam param) {
        Plan plan = planRepository.get(param.getPlanId());
        Attributes attributes = new Attributes();
        attributes.putAll(param.getAttributes());
        String id = planInstanceProcessor.schedule(plan, TriggerType.API, attributes, TimeUtils.currentLocalDateTime());
        return ResponseDTO.<String>builder().ok(id).build();
    }

    /**
     * api 触发对应planInstanceId下的job调度 目前只有workflow类型会用到
     */
    @Operation(summary = "触发对应job调度")
    @PostMapping(API_PLAN_INSTANCE_JOB_SCHEDULE)
    public ResponseDTO<String> scheduleJob(@RequestBody PlanInstanceJobScheduleParam param) {
        String id = planInstanceProcessor.scheduleJob(param.getInstanceId(), param.getJobId());
        return ResponseDTO.<String>builder().ok(id).build();
    }

    /**
     * 提交延迟任务
     */
    @Operation(summary = "触发对应plan调度")
    @PostMapping(API_DELAY_INSTANCE_COMMIT)
    public ResponseDTO<String> commitDelayInstance(@RequestBody DelayInstanceCommitParam.StandaloneParam param) {
        WorkflowJobInfo jobInfo = JobParamConverter.createJob(param);
        DAG<WorkflowJobInfo> dag = new DAG<>(Collections.singletonList(jobInfo));
        String id = idGenerator.generateId(IDType.INSTANCE);
        DelayInstance delayInstance = InstanceFactory.create(id, param.getBizType(), param.getBizId(), InstanceType.DELAY_STANDALONE, null, param.getTriggerAt(), dag);
        delayInstanceProcessor.schedule(delayInstance);
        return ResponseDTO.<String>builder().ok(id).build();
    }

}
