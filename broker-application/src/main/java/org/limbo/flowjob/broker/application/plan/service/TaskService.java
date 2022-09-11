package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.application.plan.component.JobScheduler;
import org.limbo.flowjob.broker.application.plan.manager.PlanManager;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private TaskEntityRepo taskEntityRepo;
    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;
    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private JobScheduler scheduler;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanManager planManager;


    /**
     * Worker任务执行反馈
     *
     * @param param 反馈参数
     */
    @Transactional
    public void feedback(TaskFeedbackParam param) {
        // 获取实例
        Optional<TaskEntity> taskOptional = taskEntityRepo.findById(Long.valueOf(param.getTaskId()));
        Verifies.notNull(taskOptional.isPresent(), "Task not exist!");

        TaskEntity task = taskOptional.get();

        ExecuteResult result = ExecuteResult.parse(param.getResult());
        switch (result) {
            case SUCCEED:
                taskSuccess(task.getId(), task.getJobInstanceId(), param);
                break;

            case FAILED:
                taskFail(task.getId(), task.getJobInstanceId(), param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");
        }
    }

    @Transactional
    public void taskSuccess(Long taskId, Long jobInstanceId, TaskFeedbackParam param) {
        // todo 更新plan上下文

        int num = taskEntityRepo.updateSuccessStatus(taskId,
                TaskStatus.EXECUTING.status,
                TaskStatus.SUCCEED.status,
                JacksonUtils.toJSONString(param.getResultAttributes())
        );

        if (num != 1) {
            return;
        }
        checkJobAndDispatch(jobInstanceId);
    }

    @Transactional
    public void taskFail(Long taskId, Long jobInstanceId, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.updateStatusWithError(Collections.singletonList(taskId),
                TaskStatus.EXECUTING.status,
                TaskStatus.FAILED.status,
                errorMsg,
                errorStackTrace
        );

        if (num != 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }
        checkJobAndDispatch(jobInstanceId);
    }

    @Transactional
    public void checkJobAndDispatch(Long jobInstanceId) {
        // 加锁
        jobInstanceEntityRepo.selectForUpdate(jobInstanceId);
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceId(jobInstanceId);
        for (TaskEntity taskEntity : taskEntities) {
            if (!TaskStatus.parse(taskEntity.getStatus()).isCompleted()) {
                return;
            }
        }

        JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId.toString());

        if (jobInstance.isTerminateWithFail()) {

            jobInstanceEntityRepo.updateStatus(
                    jobInstanceId,
                    JobStatus.EXECUTING.status,
                    JobStatus.FAILED.status
            );

            if (jobInstance.retry()) {
                jobInstanceRepository.save(jobInstance);
                scheduler.schedule(jobInstance);
            } else {
                planInstanceEntityRepo.end(
                        Long.valueOf(jobInstance.getPlanInstanceId()),
                        PlanStatus.EXECUTING.status,
                        PlanStatus.FAILED.status,
                        TimeUtil.currentLocalDateTime()
                );
            }
        } else {
            jobInstanceEntityRepo.updateStatus(
                    Long.valueOf(jobInstance.getJobInstanceId()),
                    JobStatus.EXECUTING.status,
                    JobStatus.SUCCEED.status
            );

            PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
            planManager.dispatchNext(planInstance, jobInstance.getJobId());
        }
    }

}
