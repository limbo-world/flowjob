package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.clent.param.SubTaskParam;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.application.plan.component.JobScheduler;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.domain.factory.JobInstanceFactory;
import org.limbo.flowjob.broker.core.domain.factory.TaskFactory22;
import org.limbo.flowjob.broker.core.domain.handler.JobFailHandler;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.JobExecuteException;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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
    private WorkerSelector workerSelector;
    @Setter(onMethod_ = @Inject)
    private JobInstanceFactory jobInstanceFactory;


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

        ExecuteResult result = param.getResult();
        switch (result) {
            case SUCCEED:
                taskSuccess(task, param);
                break;

            case FAILED:
                taskFail(task, param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");
        }
    }

    @Transactional
    public void taskFail(Task task, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.updateStatusWithError(Long.valueOf(task.getTaskId()),
                TaskStatus.EXECUTING.status,
                TaskStatus.FAILED.status,
                errorMsg,
                errorStackTrace
        );

        if (num != 1) {
            return; // 并发更新过了
        }

        num = jobInstanceEntityRepo.updateStatus(
                Long.valueOf(task.getJobInstanceId()),
                JobStatus.EXECUTING.status,
                JobStatus.FAILED.status
        );

        if (num != 1) {
            return; // 其它task已经更新job为失败 并已经执行了后续操作
        }

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());
        if (jobInstance.retry()) {
            jobInstance.setJobInstanceId(null);
            jobInstance.setStatus(JobStatus.SCHEDULING);
            jobInstanceRepository.save(jobInstance);
            scheduler.schedule(jobInstance);
        } else {
            // 执行失败处理
            JobFailHandler failHandler = jobInstance.getFailHandler();
            try {
                failHandler.handle();
            } catch (JobExecuteException e) {
                log.error("[JobFailHandler]execute error jobInstance:{}", jobInstance, e);
            }
            PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
            if (failHandler.terminate()) {
                planInstanceEntityRepo.end(
                        Long.valueOf(planInstance.getPlanInstanceId()),
                        PlanStatus.EXECUTING.status,
                        PlanStatus.FAILED.status,
                        TimeUtil.currentLocalDateTime()
                );
            } else {
                dispatchNext(planInstance, jobInstance.getJobId());
            }
        }
    }

    @Transactional
    public void taskSuccess(Task task, TaskExecuteFeedbackParam param) {
        int num = taskEntityRepo.updateStatus(Long.valueOf(task.getTaskId()),
                TaskStatus.EXECUTING.status,
                TaskStatus.SUCCEED.status
        );

        if (num != 1) {
            return;
        }

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());


        switch (task.getType()) {
            case NORMAL:
            case BROADCAST:
            case REDUCE:
                checkAndFinishJobSuccess(jobInstance);
                break;
            case MAP:
                dispatchSubTasks(jobInstance, param);
                break;
            case SUB:
                dispatchReduceTask(jobInstance, param);
                break;
        }

    }


    public void dispatchSubTasks(JobInstance instance, TaskExecuteFeedbackParam param) {
        for (SubTaskParam subTaskParam : param.getSubTasks()) {
            Task task = TaskFactory22.create(instance, TaskType.SUB);
            task.setAttributes(null); // todo 从返回值里面获取

            taskRepository.save(task);

            dispatchTask(task);
        }
    }

    public void dispatchReduceTask(JobInstance instance, TaskExecuteFeedbackParam param) {
        Task reduceTask = TaskFactory22.create(instance, TaskType.REDUCE);
        reduceTask.setAttributes(null); // todo 从返回值里面获取

        taskRepository.save(reduceTask);

        dispatchTask(reduceTask);

    }

    public void checkAndFinishJobSuccess(JobInstance jobInstance) {
        // 检查job下task是否都成功
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceId(Long.valueOf(jobInstance.getJobInstanceId()));
        boolean success = true;
        for (TaskEntity taskEntity : taskEntities) {
            if (TaskStatus.SUCCEED != TaskStatus.parse(taskEntity.getStatus())) {
                success = false;
                break;
            }
        }

        // 如果有不是success状态的，可能还在下发或者处理中，或者fail（交由fail监听处理）

        if (!success) {
            return;
        }

        int num = jobInstanceEntityRepo.updateStatus(
                Long.valueOf(jobInstance.getJobInstanceId()),
                JobStatus.EXECUTING.status,
                JobStatus.SUCCEED.status
        );

        if (num != 1) {
            return;
        }

        PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
        dispatchNext(planInstance, jobInstance.getJobId());
    }

    private void dispatchTask(Task task) {
        try {
            // dispatch task don't close transaction
            task.dispatch(workerSelector);
        } catch (Exception e) {
            log.error("TaskFeedback dispatchTask Fail task:{}", task, e);
        }
    }

    /**
     * 下发后续任务
     */
    public void dispatchNext(PlanInstance planInstance, String jobId) {
        DAG<JobInfo> dag = planInstance.getDag();
        // 当前节点的子节点
        List<JobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 检测 Plan 实例是否已经执行完成
            if (checkFinished(planInstance.getPlanInstanceId(), dag.getLeafNodes())) {
                planInstanceEntityRepo.end(
                        Long.valueOf(planInstance.getPlanInstanceId()),
                        PlanStatus.EXECUTING.status,
                        PlanStatus.SUCCEED.status,
                        TimeUtil.currentLocalDateTime()
                );
            }
        } else {

            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (JobInfo subJobInfo : subJobInfos) {
                if (checkFinished(planInstance.getPlanInstanceId(), dag.preNodes(subJobInfo.getId()))) {
                    subJobInstances.add(jobInstanceFactory.create(planInstance.getPlanInstanceId(), subJobInfo, TimeUtil.currentLocalDateTime()));
                }
            }

            for (JobInstance subJobInstance : subJobInstances) {
                jobInstanceRepository.save(subJobInstance);
            }

            for (JobInstance subJobInstance : subJobInstances) {
                scheduler.schedule(subJobInstance);
            }

        }
    }

    private boolean checkFinished(String planInstanceId, List<JobInfo> jobInfos) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }
        for (JobInfo jobInfo : jobInfos) {
            List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanInstanceIdaAndJobId(Long.valueOf(planInstanceId), Long.valueOf(jobInfo.getId()));
            // todo 获取 JobInstance
            JobInstanceEntity entity = new JobInstanceEntity();
            if (entity.getStatus() == JobStatus.SUCCEED.status) {
                return true;
            }
            if (entity.getStatus() == JobStatus.FAILED.status) {
                JobFailHandler failHandler = null;
                return failHandler == null || !failHandler.terminate();
            }
            return false;
        }

        return true;
    }

}
