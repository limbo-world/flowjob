package org.limbo.flowjob.broker.application.plan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.application.plan.component.JobScheduler;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.domain.factory.JobInstanceFactory;
import org.limbo.flowjob.broker.core.domain.factory.TaskFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.core.repository.TasksRepository;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private TasksRepository tasksRepository;
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
    private PlanInfoEntityRepo planInfoEntityRepo;
    @Setter(onMethod_ = @Inject)
    private WorkerSelector workerSelector;
    @Setter(onMethod_ = @Inject)
    private JobInstanceFactory jobInstanceFactory;
    @Setter(onMethod_ = @Inject)
    private WorkerManager workerManager;


    /**
     * Worker任务执行反馈
     *
     * @param param 反馈参数
     */
    @Transactional
    public void feedback(TaskFeedbackParam param) {
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
        int num = jobInstanceEntityRepo.updateStatus(
                Long.valueOf(task.getJobInstanceId()),
                JobStatus.EXECUTING.status,
                JobStatus.FAILED.status
        );

        if (num != 1) {
            return; // 其它task已经更新job为失败 并已经执行了后续操作
        }

        num = taskEntityRepo.updateStatusWithError(Long.valueOf(task.getTaskId()),
                TaskStatus.EXECUTING.status,
                TaskStatus.FAILED.status,
                errorMsg,
                errorStackTrace
        );

        if (num != 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());
        if (jobInstance.retry()) {
            jobInstanceRepository.save(jobInstance);
            scheduler.schedule(jobInstance);
        } else {
            PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
            if (jobInstance.isTerminateWithFail()) {
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
    public void taskSuccess(Task task, TaskFeedbackParam param) {
        // todo 更新plan上下文

        int num = taskEntityRepo.updateSuccessStatus(Long.valueOf(task.getTaskId()),
                TaskStatus.EXECUTING.status,
                TaskStatus.SUCCEED.status,
                JacksonUtils.toJSONString(param.getResultAttributes())
        );

        if (num != 1) {
            return;
        }

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());

        switch (task.getType()) {
            case NORMAL:
            case BROADCAST:
            case REDUCE:
                boolean success = isFinished(jobInstance, task.getType());
                // 如果有不是success状态的，可能还在下发或者处理中，或者fail（交由fail回调处理）
                if (!success) {
                    break;
                }
                jobSuccess(jobInstance);
                break;
            case SPLIT:
                dispatchSubTasks(jobInstance, param.getMapTaskAttributes().stream().map(Attributes::new).collect(Collectors.toList()));
                break;
            case MAP:
                checkAndDispatchReduceTask(jobInstance);
                break;
        }

    }


    @Transactional
    public void dispatchSubTasks(JobInstance instance, List<Attributes> mapTaskAttributes) {
        List<Task> subTasks = new ArrayList<>();
        for (Attributes mapTaskAttribute : mapTaskAttributes) {
            Task task = TaskFactory.create(instance, TaskType.MAP);
            task.setAttributes(mapTaskAttribute);

            subTasks.add(task);
        }
        JobInstance.Tasks tasks = new JobInstance.Tasks(instance.getJobInstanceId(), subTasks);
        dispatch(instance, tasks);
    }

    @Transactional
    public void checkAndDispatchReduceTask(JobInstance instance) {
        // 判断 sub task 是否都执行完
        boolean finished = isFinished(instance, TaskType.MAP);
        if (!finished) {
            return;
        }

        // 获取所有的splitTask
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(Long.valueOf(instance.getJobInstanceId()), TaskType.SPLIT.type);
        List<Attributes> reduceAttributes = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            reduceAttributes.add(new Attributes(JacksonUtils.parseObject(taskEntity.getResult(), new TypeReference<Map<String, Object>>() {
            })));
        }

        Task reduceTask = TaskFactory.create(instance, TaskType.REDUCE);
        reduceTask.setReduceAttributes(reduceAttributes);

        JobInstance.Tasks tasks = new JobInstance.Tasks(instance.getJobInstanceId(), Collections.singletonList(reduceTask));
        dispatch(instance, tasks);

    }

    public boolean isFinished(JobInstance jobInstance, TaskType type) {
        // 检查job下task是否都成功
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(Long.valueOf(jobInstance.getJobInstanceId()), type.type);
        boolean success = true;
        for (TaskEntity taskEntity : taskEntities) {
            if (TaskStatus.SUCCEED != TaskStatus.parse(taskEntity.getStatus())) {
                success = false;
                break;
            }
        }
        return success;
    }

    @Transactional
    public void jobSuccess(JobInstance jobInstance) {
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

    @Transactional
    public void dispatch(JobInstance jobInstance, JobInstance.Tasks tasks) {

        tasksRepository.save(tasks);

        // 下发
        for (Task task : tasks.getTasks()) {

            // 防止重复下发
            jobInstance.dispatch(task);

            // 保存数据
            if (TaskStatus.EXECUTING == task.getStatus()) { // 成功
                taskEntityRepo.updateStatus(Long.valueOf(task.getTaskId()),
                        TaskStatus.DISPATCHING.status,
                        TaskStatus.EXECUTING.status,
                        task.getWorkerId()
                );
            } else { // 失败
                taskFail(task, "task dispatch fail", "");
            }
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
            if (checkFinished(planInstance.getPlanInstanceId(), dag.lasts())) {
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
            List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanInstanceIdAndJobId(Long.valueOf(planInstanceId), Long.valueOf(jobInfo.getId()));
            // todo 获取 JobInstance
            JobInstanceEntity entity = new JobInstanceEntity();
            if (entity.getStatus() == JobStatus.SUCCEED.status) {
                return true;
            }
            if (entity.getStatus() == JobStatus.FAILED.status) {
                return !jobInfo.isTerminateWithFail();
            }
            return false;
        }

        return true;
    }

}
