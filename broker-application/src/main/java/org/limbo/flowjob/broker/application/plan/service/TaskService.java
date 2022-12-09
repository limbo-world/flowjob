package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.application.plan.manager.PlanScheduleManager;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.ExecuteResult;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Brozen
 * @since 2022-06-24
 */
@Slf4j
@Service
public class TaskService {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanScheduleManager planScheduleManager;


    /**
     * Worker任务执行反馈
     *
     * @param taskId 任务id
     * @param param  反馈参数
     */
    @Transactional
    public void feedback(String taskId, TaskFeedbackParam param) {
        // 获取实例
        TaskEntity task = taskEntityRepo.findById(taskId)
                .orElseThrow(() -> new VerifyException("Task not exist! id:" + taskId));

        ExecuteResult result = ExecuteResult.parse(param.getResult());

        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        switch (result) {
            case SUCCEED:
                handleTaskSuccess(task.getTaskId(), task.getJobInstanceId(), param);
                break;

            case FAILED:
                handleTaskFail(task.getTaskId(), task.getJobInstanceId(), param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

    @Transactional
    public void handleTaskSuccess(String taskId, String jobInstanceId, TaskFeedbackParam param) {
        // todo 更新plan上下文

        int num = taskEntityRepo.updateStatusSuccess(taskId, JacksonUtils.toJSONString(param.getResultAttributes()));

        if (num != 1) { // 已经被更新 无需重复处理
            return;
        }
        handleJobStatus(jobInstanceId);
    }

    @Transactional
    public void handleTaskFail(String taskId, String jobInstanceId, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.updateStatusFail(taskId, errorMsg, errorStackTrace);

        if (num != 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }
        handleJobStatus(jobInstanceId);
    }

    //    @Transactional
    private void handleJobStatus(String jobInstanceId) {
        // 加锁
        jobInstanceEntityRepo.selectForUpdate(jobInstanceId);
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceId(jobInstanceId);
        for (TaskEntity taskEntity : taskEntities) {
            if (!TaskStatus.parse(taskEntity.getStatus()).isCompleted()) {
                return; // 如果还未完成 交由最后完成的task去做后续逻辑处理
            }
        }

        JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId);
        // 判断状态是不是已经更新 可能已经被其它线程处理
        if (JobStatus.EXECUTING != jobInstance.getStatus()) {
            return;
        }

        // 如果所有task都是执行成功 则处理成功
        // 如果所有task都是执行失败 则处理失败
        boolean success = taskEntities.stream().allMatch(entity -> TaskStatus.SUCCEED == TaskStatus.parse(entity.getStatus()));
        if (success) {
            handleJobSuccess(jobInstance);
        } else {
            handleJobFail(jobInstance);
        }

    }

    //    @Transactional
    private void handleJobSuccess(JobInstance jobInstance) {
        jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());

        PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
        planScheduleManager.dispatchNext(planInstance, jobInstance.getJobId());
    }

    //    @Transactional
    private void handleJobFail(JobInstance jobInstance) {
        if (jobInstance.isTerminateWithFail()) {

            jobInstanceEntityRepo.updateStatusExecuteFail(jobInstance.getJobInstanceId(), MsgConstants.TASK_FAIL);

            if (jobInstance.retry()) {
                jobInstanceRepository.save(jobInstance);
                planScheduleManager.dispatch(jobInstance);
            } else {
                planInstanceEntityRepo.fail(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
            }
        } else {
            handleJobSuccess(jobInstance);
        }
    }

}
