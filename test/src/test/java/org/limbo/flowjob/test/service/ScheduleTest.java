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
import org.limbo.flowjob.broker.application.controller.WorkerRpcController;
import org.limbo.flowjob.broker.application.service.PlanService;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.PlanScheduleTask;
import org.limbo.flowjob.broker.core.schedule.strategy.IPlanScheduleStrategy;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerConverter;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.PlanType;
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
import javax.transaction.Transactional;
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
    private IPlanScheduleStrategy planScheduleStrategy;

    @MockBean
    private TaskDispatcher taskDispatcher;

    @MockBean
    private SlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

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

    @BeforeEach
    public void before(){
        // mock work rpc
        Mockito.doAnswer((Answer<Void>) m -> null).when(brokerRpc).register(Mockito.any(Worker.class));
        Mockito.doAnswer((Answer<Void>) m -> null).when(brokerRpc).heartbeat(Mockito.any(Worker.class));
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) {
                Object[] arguments = invocationOnMock.getArguments();
                ExecuteContext context = (ExecuteContext) arguments[0];
                TaskFeedbackParam taskFeedbackParam = RpcParamFactory.taskFeedbackParam(context.getTask().getContext(), context.getTask().getResult(), null);
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
        saveAndExecutePlan(param, 2, 0);
    }

    @Test
//    @Transactional
    void testNormalSinglePlanSuccess() {
        PlanParam param = PlanParamFactory.newFixedRateAddParam(PlanType.SINGLE);
        saveAndExecutePlan(param, 1, 0);
    }

    @Test
//    @Transactional
    void testMapReduceWorkflowPlanSuccess() {
        PlanParam param = PlanParamFactory.newMapReduceAddParam(PlanType.WORKFLOW);
        saveAndExecutePlan(param, 2, 0);
    }

    @Test
//    @Transactional
    void testMapReduceSinglePlanSuccess() {
        PlanParam param = PlanParamFactory.newMapReduceAddParam(PlanType.SINGLE);
        saveAndExecutePlan(param, 1, 0);
    }

    private void saveAndExecutePlan(PlanParam param, int expectSuccess, int expectFail) {
        String id = planService.save(null, param);
        planService.start(id);

        PlanEntity planEntity = planEntityRepo.findById(id).orElse(null);
        PlanScheduleTask planScheduleTask = domainConverter.toPlanScheduleTask(planEntity);
        // 调度plan 生成 task 并下发
        Plan plan = planScheduleTask.getPlan();
        planScheduleStrategy.schedule(plan.getTriggerType(), plan, TimeUtils.currentLocalDateTime());

        try {
            // 等待执行完成
            Thread.sleep(5000);

            long executing = jobInstanceEntityRepo.countByPlanIdAndStatusIn(id, Lists.newArrayList(JobStatus.SCHEDULING.status, JobStatus.EXECUTING.status));
            assert executing == 0;

            long success = jobInstanceEntityRepo.countByPlanIdAndStatusIn(id, Lists.newArrayList(JobStatus.SUCCEED.status));
            assert success == expectSuccess;

            long failed = jobInstanceEntityRepo.countByPlanIdAndStatusIn(id, Lists.newArrayList(JobStatus.FAILED.status));
            assert failed == expectFail;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
