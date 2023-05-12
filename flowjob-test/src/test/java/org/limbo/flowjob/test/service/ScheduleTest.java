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

package org.limbo.flowjob.test.service;

import com.google.common.collect.Lists;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.api.remote.param.TaskSubmitParam;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.schedule.ScheduleStrategy;
import org.limbo.flowjob.broker.application.task.PlanScheduleTask;
import org.limbo.flowjob.broker.application.controller.WorkerRpcController;
import org.limbo.flowjob.broker.application.converter.MetaTaskConverter;
import org.limbo.flowjob.broker.application.service.PlanService;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.limbo.flowjob.test.support.PlanParamFactory;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;
import org.limbo.flowjob.worker.core.rpc.RpcParamFactory;
import org.limbo.flowjob.worker.starter.application.services.WorkerService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ~OttO~
 * @date 2023/1/10
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ScheduleTest {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategy scheduleStrategy;

    @MockBean
    private TaskDispatcher taskDispatcher;

    @MockBean
    private SlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private MetaTaskConverter metaTaskConverter;

    @MockBean
    protected MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @MockBean
    private BrokerRpc brokerRpc;

    @Named("fjwWorkerService")
    @Setter(onMethod_ = @Inject)
    private WorkerService workerService;

    @Setter(onMethod_ = @Inject)
    private WorkerRpcController workerRpcController;

    private static String PLAN_INSTANCE_ID = null;

    @BeforeEach
    public void before() {
        // mock work rpc
        Mockito.doAnswer((Answer<Void>) m -> null).when(brokerRpc).register(Mockito.any(Worker.class));
        Mockito.doAnswer((Answer<Void>) m -> null).when(brokerRpc).heartbeat(Mockito.any(Worker.class));
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) {
                Object[] arguments = invocationOnMock.getArguments();
                ExecuteContext context = (ExecuteContext) arguments[0];
                TaskFeedbackParam taskFeedbackParam = RpcParamFactory.taskFeedbackParam(context.getTask().getContext(),
                        context.getTask().getJobAttributes(),
                        context.getTask().getResult(),
                        null
                );
                workerRpcController.feedback(context.getTask().getTaskId(), taskFeedbackParam);
                return null;
            }
        }).when(brokerRpc).feedbackTaskSucceed(Mockito.any(ExecuteContext.class));

        // mock slot
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < SlotManager.SLOT_SIZE; i++) {
            slots.add(i);
        }
        Mockito.when(slotManager.slots()).thenReturn(slots);

        // mock task dispatcher
        Mockito.doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) {
                Object[] arguments = invocationOnMock.getArguments();
                Task task = (Task) arguments[0];
                task.setWorkerId("123");

                TaskSubmitParam taskSubmitParam = WorkerConverter.toTaskSubmitParam(task);
                workerService.receive(taskSubmitParam);

                PLAN_INSTANCE_ID = taskSubmitParam.getPlanInstanceId();
                return true;
            }
        }).when(taskDispatcher).dispatch(Mockito.any(Task.class));

        // mock scheduler
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) {
                Object[] arguments = invocationOnMock.getArguments();
                MetaTask metaTask = (MetaTask) arguments[0];
                metaTask.execute();
                return null;
            }
        }).when(metaTaskScheduler).schedule(Mockito.any(MetaTask.class));
    }

    @Test
//    @Transactional --- 加上 由于不落表 只在当前事务内 和 before 里面的产生冲突 before 里面获取不到数据
    void testNormalWorkflowPlanSuccess() {
        PlanParam param = PlanParamFactory.newFixedRateAddParam(PlanType.WORKFLOW);
        saveAndExecutePlan(TriggerType.SCHEDULE, param, 2, 2, 0);
    }

    @Test
//    @Transactional
    void testNormalPlanSuccess() {
        PlanParam param = PlanParamFactory.newFixedRateAddParam(PlanType.NORMAL);
        saveAndExecutePlan(TriggerType.SCHEDULE, param, 1, 1, 0);
    }

    @Test
//    @Transactional
    void testMapReduceWorkflowPlanSuccess() {
        PlanParam param = PlanParamFactory.newMapReduceAddParam(PlanType.WORKFLOW);
        saveAndExecutePlan(TriggerType.SCHEDULE, param, 2, 2, 0);
    }

    @Test
//    @Transactional
    void testMapReducePlanSuccess() {
        PlanParam param = PlanParamFactory.newMapReduceAddParam(PlanType.NORMAL);
        saveAndExecutePlan(TriggerType.SCHEDULE, param, 1, 1, 0);
    }

    @Test
    void testApiPlanSuccess() {
        PlanParam param = PlanParamFactory.newWorkflowParam(TriggerType.API);
        saveAndExecutePlan(TriggerType.SCHEDULE, param, 2, 2, 0);
    }

    private void saveAndExecutePlan(TriggerType triggerType, PlanParam param, int total, int expectSuccess, int expectFail) {
        String id = planService.save(null, param);
        planService.start(id);

        PlanEntity planEntity = planEntityRepo.findById(id).orElse(null);
        PlanScheduleTask planScheduleTask = metaTaskConverter.toPlanScheduleTask(planEntity.getPlanId(), triggerType);
        // 调度plan 生成 task 并下发
        Plan plan = planScheduleTask.getPlan();
        scheduleStrategy.schedule(triggerType, plan, TimeUtils.currentLocalDateTime());

        long finished = 0;
        long success = 0;
        long failed = 0;
        while (finished < total) {
            success = jobInstanceEntityRepo.countByPlanInstanceIdAndStatusIn(PLAN_INSTANCE_ID, Lists.newArrayList(JobStatus.SUCCEED.status));
            failed = jobInstanceEntityRepo.countByPlanInstanceIdAndStatusIn(PLAN_INSTANCE_ID, Lists.newArrayList(JobStatus.FAILED.status));
            finished = success + failed;

            if (PlanType.WORKFLOW == plan.getType()) {
                WorkflowPlan workflowPlan = (WorkflowPlan) plan;
                DAG<WorkflowJobInfo> dag = workflowPlan.getDag();
                List<WorkflowJobInfo> nodes = dag.nodes();
                for (WorkflowJobInfo jobInfo : nodes) {
                    if (TriggerType.API == jobInfo.getTriggerType()) {
                        try {
                            workerRpcController.scheduleJob(PLAN_INSTANCE_ID, jobInfo.getId());
                        } catch (VerifyException e) {
                            if ("".equals(e.getMessage())) {
                                // 不处理
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assert success == expectSuccess;
        assert failed == expectFail;
    }

}
