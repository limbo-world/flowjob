package org.limbo.flowjob.tracker.core.job.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.*;
import org.limbo.flowjob.tracker.core.job.handler.JobFailHandler;
import org.limbo.flowjob.tracker.core.plan.*;
import org.limbo.flowjob.tracker.core.storage.Storage;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class ClosedConsumer implements Consumer<Task> {

    private final TaskRepository taskRepository;

    private final PlanRecordRepository planRecordRepository;

    private final PlanInstanceRepository planInstanceRepository;

    private final JobRecordRepository jobRecordRepository;

    private final JobInstanceRepository jobInstanceRepository;

    private final PlanRepository planRepository;

    private final TrackerNode trackerNode;

    private final Storage storage;

    public ClosedConsumer(TaskRepository taskRepository,
                          PlanInstanceRepository planInstanceRepository,
                          PlanRepository planRepository,
                          TrackerNode trackerNode,
                          Storage storage) {
        this.taskRepository = taskRepository;
        this.planInstanceRepository = planInstanceRepository;
        this.planRepository = planRepository;
        this.trackerNode = trackerNode;
        this.storage = storage;
    }

    @Override
    public void accept(Task task) {
        if (log.isDebugEnabled()) {
            log.debug(task.getWorkerId() + " closed " + task.getId());
        }
        taskRepository.end(task);

        switch (task.getState()) {
            case SUCCEED:
                handlerSuccess(task);
                break;

            case FAILED:
                handlerFailed(task);
                break;
        }
    }

    public void handlerSuccess(Task task) {
        handlerShardingTaskSuccess();
        handlerNormalTaskSuccess(task);
    }

    public void handlerShardingTaskSuccess() {
        // todo 分页任务 从返回值获取分出来的task并下发
    }

    public void handlerNormalTaskSuccess(Task task) {
        // 判断是否所有 task 成功
        if (taskRepository.unclosedTaskCount(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(), task.getJobInstanceId()) > 0) {
            return;
        }
        // todo
        jobInstanceRepository.end(null);
        jobRecordRepository.end();

        Plan plan = planRepository.getPlan(task.getPlanId(), task.getVersion());

        List<Job> subJobs = plan.getDag().getSubJobs(task.getJobId());
        if (CollectionUtils.isEmpty(subJobs)) {
            // 无后续节点，需要判断是否plan结束
            if (checkPreJobsFinished(plan.getPlanId(), task.getPlanInstanceId(), plan.getDag().getFinalJobs())) {
                LocalDateTime endTime = TimeUtil.nowLocalDateTime();
                // 结束plan
                planInstanceRepository.end(planInstance.getPlanId(), planInstance.getPlanInstanceId(), endTime, PlanScheduleStatus.SUCCEED);
                planRecordRepository.end(planInstance.getPlanId(), planInstance.getPlanInstanceId(), endTime, PlanScheduleStatus.SUCCEED);

                // 判断 plan 是否需要 feedback 只有 FIXED_INTERVAL类型需要反馈，让任务在时间轮里面能重新下发，手动的和其他的都不需要
                plan.setLastScheduleAt(planInstance.getStartAt());
                plan.setLastFeedBackAt(TimeUtil.toInstant(endTime));
                if (ScheduleType.FIXED_INTERVAL == plan.getScheduleOption().getScheduleType() && planInstance.isReschedule()) {
                    trackerNode.jobTracker().schedule(plan);
                }
            }
        } else {
            // 不为end节点，继续下发
            for (Job job : subJobs) {
                if (checkPreJobsFinished(plan.getPlanId(), planInstance.getPlanInstanceId(), plan.getDag().getPreJobs(job.getJobId()))) {
                    storage.store(job.newInstance(plan.getPlanId(), planInstance.getPlanInstanceId(), plan.getVersion(), JobScheduleStatus.Scheduling));
                }
            }
        }
    }


    public void handlerFailed(Task task) {
        // 获取jobInstance
        JobInstance jobInstance = null;
        JobRecord jobRecord = null;
        int retry =3;

        // 此次jobinstance失败
        jobInstanceRepository.end(null);

        // 如果 jobRecord 可以重试，重新下发一个jobinstance
        if (retry > 0) {
            storage.store(jobRecord);
            return;
        }

        // 超过重试次数 执行handler
        JobFailHandler failHandler = task.getFailHandler();
        try {
            failHandler.handle();
            handlerSuccess(task);
        } catch (JobExecuteException e) {
            PlanRecord planRecord = null;

            // 此次 planInstance 失败
            planInstanceRepository.end(planInstance.getPlanId(), planInstance.getPlanInstanceId(),
                    TimeUtil.nowLocalDateTime(), PlanScheduleStatus.FAILED);

            // 如果 planRecord 可以重试，重新下发一个 jobinstance
            if (retry > 0) {
                storage.store(planRecord);
                return;
            }

            // 如果超过重试次数 此planRecord失败
            planRecordRepository.end(planInstance.getPlanId(), planInstance.getPlanInstanceId(),
                    TimeUtil.nowLocalDateTime(), PlanScheduleStatus.FAILED);
        }

    }

    /**
     * 判断前置节点是否完成
     * @param planId
     * @param planInstanceId
     * @param preJobs
     * @return
     */
    public boolean checkPreJobsFinished(String planId, Long planInstanceId, List<Job> preJobs) {
        // 获取db中 job实例
        List<Task> finalInstances = jobInstanceRepository.getInstances(planId, planInstanceId,
                preJobs.stream().map(Job::getJobId).collect(Collectors.toList()));

        // 有实例还未创建直接返回
        if (preJobs.size() > finalInstances.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (Task finalInstance : finalInstances) {
            if (!finalInstance.canTriggerNext()) {
                return false;
            }
        }
        return true;
    }

}
