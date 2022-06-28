package org.limbo.flowjob.broker.application.plan.service;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanSchedulerRepository;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2022-06-24
 */
@Slf4j
@Service
public class TaskService {

    @Inject
    private TaskRepository taskRepository;

    @Inject
    private PlanSchedulerRepository planSchedulerRepo;

    @Inject
    private PlanInstanceRepository planInstRepo;

    @Inject
    private JobInstanceRepository jobInstanceRepo;


    /**
     * Worker任务执行反馈
     *
     * @param param 反馈参数
     */
    public void feedback(TaskExecuteFeedbackParam param) {
        // 获取实例
        Task task = taskRepository.get(param.getTaskId());
        Verifies.notNull(task, "Task not exist!");

        PlanInstance planInst = planInstRepo.get(task.getPlanInstanceId());
        Verifies.notNull(planInst, "Plan instance not exist!");

        JobInstance jobInst = jobInstanceRepo.get(task.getJobInstanceId());
        Verifies.notNull(planInst, "Job instance not exist!");

        PlanScheduler scheduler = planSchedulerRepo.get(planInst.getVersion());
        Verifies.notNull(planInst, "Schedulable plan not exist in current broker!");


        // 变更状态
        ExecuteResult result = param.getResult();
        switch (result) {
            case SUCCEED:
                task.succeed(scheduler, planInst, jobInst);
                break;

            case FAILED:
                task.failed(scheduler, planInst, jobInst, param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");
        }
    }
}
