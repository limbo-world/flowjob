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

package org.limbo.flowjob.broker.application.schedule;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.param.broker.TaskFeedbackParam;
import org.limbo.flowjob.broker.application.converter.MetaTaskConverter;
import org.limbo.flowjob.broker.application.task.TaskScheduleTask;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2023/2/8
 */
@Slf4j
@Component
public class ScheduleStrategy implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceService planInstanceService;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    private MetaTaskConverter metaTaskConverter;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    private final Map<PlanType, PlanScheduler> schedulers = new EnumMap<>(PlanType.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        Map<String, PlanScheduler> map = applicationContext.getBeansOfType(PlanScheduler.class);
        if (MapUtils.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, PlanScheduler> entry : map.entrySet()) {
            PlanScheduler scheduler = entry.getValue();
            schedulers.put(scheduler.getPlanType(), scheduler);
        }
    }

    /**
     * 创建PlanInstance并执行调度
     */
    public void schedule(TriggerType triggerType, Plan plan, LocalDateTime triggerAt) {
        // 悲观锁快速释放，不阻塞后续任务
        String planInstanceId = idGenerator.generateId(IDType.PLAN_INSTANCE);
        planInstanceService.save(planInstanceId, triggerType, plan, triggerAt);
        schedule(planInstanceId, plan, triggerAt);
    }

    /**
     * 调度已有的PlanInstance
     */
    public void schedule(String planInstanceId, Plan plan, LocalDateTime triggerAt) {
        executeWithAspect(unused -> schedulers.get(plan.getType()).schedule(plan, planInstanceId, triggerAt));
    }

    /**
     * 调度已有的PlanInstance
     */
    public void schedule(JobInstance jobInstance) {
        executeWithAspect(unused -> schedulers.get(jobInstance.getPlanType()).schedule(jobInstance));
    }

    /**
     * api 方式下发节点任务
     */
    public void scheduleJob(String planInstanceId, String jobId, boolean retry) {
        executeWithAspect(unused -> {
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId));
            Plan plan = planRepository.getByVersion(planInstanceEntity.getPlanId(), planInstanceEntity.getPlanInfoId());schedulers.get(plan.getType()).scheduleJob(plan, planInstanceId, jobId, retry);
        });
    }

    /**
     * Worker任务执行反馈
     *
     * @param taskId 任务id
     * @param param  反馈参数
     */
    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        executeWithAspect(unused -> {
            ExecuteResult result = param.getResult();
            if (log.isDebugEnabled()) {
                log.debug("receive task feedback id:{} result:{}", taskId, result);
            }

            TaskEntity taskEntity = taskEntityRepo.findById(taskId).orElse(null);
            Verifies.notNull(taskEntity, "task is null id:" + taskId);

            Task task = DomainConverter.toTask(taskEntity);

            PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(taskEntity.getPlanInfoId()).orElse(null);
            PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
            switch (result) {
                case SUCCEED:
                    task.setContext(new Attributes(param.getContext()));
                    task.setJobAttributes(new Attributes(param.getJobAttributes()));
                    schedulers.get(planType).handleSuccess(task, param.getResultData());
                    break;

                case FAILED:
                    schedulers.get(planType).handleFail(task, param.getErrorMsg(), param.getErrorStackTrace());
                    break;

                case TERMINATED:
                    throw new UnsupportedOperationException("暂不支持手动终止任务");

                default:
                    throw new IllegalStateException("Unexpect execute result: " + param.getResult());
            }
        });
    }

    public void schedule(Task task) {
        executeWithAspect(unused -> {
            PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(task.getPlanVersion()).orElse(null);
            PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
            schedulers.get(planType).schedule(task);
        });
    }

    public void executeWithAspect(Consumer<Void> consumer) {
        try {
            // new context
            ScheduleContext.set();
            // do real
            consumer.accept(null);
            // do after
            scheduleTasks();
        } finally {
            // clear context
            ScheduleContext.clear();
        }
    }

    /**
     * 放在事务外，防止下发和执行很快但是task下发完需要很久的情况，这样前面的任务执行返回后由于事务未提交，会提示找不到task
     */
    public void scheduleTasks() {
        if (CollectionUtils.isEmpty(ScheduleContext.waitScheduleTasks())) {
            return;
        }
        for (Task task : ScheduleContext.waitScheduleTasks()) {
            try {
                TaskScheduleTask taskScheduleTask = metaTaskConverter.toTaskScheduleTask(task);
                metaTaskScheduler.schedule(taskScheduleTask);
            } catch (Exception e) {
                // 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! task={}", task, e);
            }
        }
    }

}
