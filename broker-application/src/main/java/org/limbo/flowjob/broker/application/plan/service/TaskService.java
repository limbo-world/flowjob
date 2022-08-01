package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2022-06-24
 */
@Slf4j
@Service
public class TaskService {

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    /**
     * Worker任务执行反馈
     *
     * @param param 反馈参数
     */
    @Transactional
    public void feedback(TaskExecuteFeedbackParam param) {
        // 获取实例
        Task task = taskRepository.get(param.getTaskId());
        Verifies.notNull(task, "Task not exist!");

        PlanInstance planInstance = planInstanceRepository.get(task.getPlanInstanceId());
        Verifies.notNull(planInstance, "Plan instance not exist!");

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());
        Verifies.notNull(planInstance, "Job instance not exist!");

        ExecuteResult result = param.getResult();
        switch (result) {
            case SUCCEED:
                handlerSuccess(task, planInstance, jobInstance);
                break;

            case FAILED:
                handlerFailed(task, planInstance, jobInstance, param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");
        }
    }

    /**
     * todo 先执行db 和 后执行db的抉择
     */
    private void handlerSuccess(Task task, PlanInstance planInstance, JobInstance jobInstance) {
        taskRepository.executeSucceed(task);
        jobInstanceRepository.executeSucceed(jobInstance);

//        planInstanceRepo.executeSucceed(this);
//
//        if (ScheduleType.FIXED_DELAY == scheduleOption.getScheduleType() && TriggerType.SCHEDULE == scheduleOption.getTriggerType()) {
//            planRepository.nextTriggerAt(planId, nextTriggerAt());
//        }

        // todo 如果task或者jobInstance更新失败 -- 可能是并发下导致状态问题？？？ 无需继续下发
        planInstance.handlerTaskSuccess(jobInstance, task); // 下发后续任务
    }

    private void handlerFailed(Task task, PlanInstance planInstance, JobInstance jobInstance, String errorMsg, String errorStackTrace) {
        taskRepository.executeFailed(task);
        jobInstanceRepository.executeFailed(jobInstance);

//        planInstanceRepo.executeSucceed(this);
//
//        if (ScheduleType.FIXED_DELAY == scheduleOption.getScheduleType() && TriggerType.SCHEDULE == scheduleOption.getTriggerType()) {
//            planRepository.nextTriggerAt(planId, nextTriggerAt());
//        }

        // todo 重试下发
        task.failed(planInstance, jobInstance, errorMsg, errorStackTrace);


    }

}
