package org.limbo.flowjob.broker.test.service;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.service.PlanService;
import org.limbo.flowjob.broker.application.service.TaskService;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.PlanScheduleTask;
import org.limbo.flowjob.broker.core.schedule.strategy.ScheduleStrategyFactory;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.broker.test.support.PlanParamFactory;
import org.limbo.flowjob.common.constants.ExecuteResult;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
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
    private ScheduleStrategyFactory scheduleStrategyFactory;

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
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskService taskService;

    @Setter(onMethod_ = @Inject)
    private PlanParamFactory planParamFactory;

    @BeforeEach
    public void before(){
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < SlotManager.SLOT_SIZE; i++) {
            slots.add(i);
        }
        Mockito.doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) {
                Object[] arguments = invocationOnMock.getArguments();
                Task task = (Task) arguments[0];
                task.setWorkerId("123");
                return true;
            }
        }).when(taskDispatcher).dispatch(Mockito.any(Task.class));
        Mockito.when(slotManager.slots()).thenReturn(slots);
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
    @Transactional
    void testTaskSuccess() {
        PlanParam param = planParamFactory.newFixedRateAddParam();

        String id = planService.save(null, param);
        planService.start(id);

        PlanEntity planEntity = planEntityRepo.findById(id).orElse(null);
        PlanScheduleTask planScheduleTask = domainConverter.toPlanScheduleTask(planEntity);
        // 调度plan 生成 task 并下发
        Plan plan = planScheduleTask.getPlan();
        scheduleStrategyFactory.build(plan.planType()).schedule(plan.getTriggerType(), plan, TimeUtils.currentLocalDateTime());
        // 处理task 执行
        while (true) { // 处理所有节点
            List<TaskEntity> taskEntities = taskEntityRepo.findByPlanIdInAndStatus(Collections.singletonList(id), TaskStatus.EXECUTING.status);
            if (CollectionUtils.isEmpty(taskEntities)) {
                break;
            }
            for (TaskEntity taskEntity : taskEntities) {
                taskService.taskFeedback(taskEntity.getTaskId(), TaskFeedbackParam.builder()
                        .result(ExecuteResult.SUCCEED.result)
                        .build()
                );

                checkTaskFinish(taskEntity.getTaskId(), TaskStatus.SUCCEED);
            }
        }
    }

    private void checkTaskFinish(String id, TaskStatus status) {
        TaskEntity task = taskEntityRepo.findById(id).orElse(null);
        assert task != null;
        assert TaskStatus.parse(task.getStatus()) == status;
    }

}
