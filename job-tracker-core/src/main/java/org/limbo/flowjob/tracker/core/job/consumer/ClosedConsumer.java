package org.limbo.flowjob.tracker.core.job.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.*;
import org.limbo.flowjob.tracker.core.job.handler.JobFailHandler;
import org.limbo.flowjob.tracker.core.plan.*;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class ClosedConsumer implements Consumer<Event<?>> {

    private final TaskRepository taskRepository;

    private final PlanRecordRepository planRecordRepository;

    private final PlanInstanceRepository planInstanceRepository;

    private final JobRecordRepository jobRecordRepository;

    private final JobInstanceRepository jobInstanceRepository;

    private final PlanRepository planRepository;

    private final TrackerNode trackerNode;

    private final EventPublisher<Event<?>> eventPublisher;

    public ClosedConsumer(TaskRepository taskRepository,
                          PlanRecordRepository planRecordRepository,
                          PlanInstanceRepository planInstanceRepository,
                          PlanRepository planRepository,
                          JobRecordRepository jobRecordRepository,
                          JobInstanceRepository jobInstanceRepository,
                          TrackerNode trackerNode,
                          EventPublisher<Event<?>> eventPublisher) {
        this.taskRepository = taskRepository;
        this.planRecordRepository = planRecordRepository;
        this.planInstanceRepository = planInstanceRepository;
        this.planRepository = planRepository;
        this.jobRecordRepository = jobRecordRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.trackerNode = trackerNode;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void accept(Event<?> event) {
        if (!(event.getSource() instanceof Task)) {
            return;
        }
        Task task = (Task) event.getSource();
        switch (task.getResult()) {
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
        // 判断是否所有 task 执行过
        if (taskRepository.unclosedCount(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(), task.getJobInstanceId()) > 0) {
            return;
        }
        // 结束 job
        jobInstanceRepository.end(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(),
                task.getJobInstanceId(), JobScheduleStatus.SUCCEED);
        jobRecordRepository.end(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(),
                JobScheduleStatus.SUCCEED);

        PlanRecord planRecord = planRecordRepository.get(task.getPlanId(), task.getPlanRecordId());

        List<Job> subJobs = planRecord.getDag().getSubJobs(task.getJobId());
        if (CollectionUtils.isEmpty(subJobs)) {
            // 无后续节点，需要判断是否plan结束
            if (checkJobsFinished(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), planRecord.getDag().getFinalJobs())) {
                // 结束plan
                planInstanceRepository.end(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), PlanScheduleStatus.SUCCEED);
                planRecordRepository.end(task.getPlanId(), task.getPlanRecordId(), PlanScheduleStatus.SUCCEED);

                // 判断 plan 是否需要 重新调度 只有 FIXED_INTERVAL类型需要反馈，让任务在时间轮里面能重新下发，手动的和其他的都不需要
                Plan plan = planRepository.getPlan(task.getPlanId(), planRecord.getVersion());
                if (ScheduleType.FIXED_INTERVAL == plan.getScheduleOption().getScheduleType() && planRecord.isManual()) {
                    plan.setLastScheduleAt(planRecord.getStartAt());
                    plan.setLastFeedBackAt(TimeUtil.nowInstant());
                    trackerNode.jobTracker().schedule(plan);
                }
            }
        } else {
            // 不为end节点，继续下发
            for (Job job : subJobs) {
                if (checkJobsFinished(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), planRecord.getDag().getPreJobs(job.getJobId()))) {
                    eventPublisher.publish(new Event<>(job.newRecord(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), JobScheduleStatus.SCHEDULING)));
                }
            }
        }
    }

    public void handlerFailed(Task task) {
        PlanInstance planInstance = planInstanceRepository.get(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId());
        // 重复处理的情况
        if (planInstance.getState() == PlanScheduleStatus.SUCCEED || planInstance.getState() == PlanScheduleStatus.FAILED) {
            return;
        }

        jobInstanceRepository.end(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(),
                task.getJobInstanceId(), JobScheduleStatus.FAILED);

        JobRecord jobRecord = jobRecordRepository.get(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId());
        List<JobInstance> jobInstances = jobInstanceRepository.listByRecord(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId());
        // 判断是否超过重试次数
        if (jobRecord.getRetry() >= CollectionUtils.size(jobInstances)) {
            eventPublisher.publish(new Event<>(jobRecord));
            return;
        }

        jobRecordRepository.end(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(),
                JobScheduleStatus.FAILED);

        // 超过重试次数 执行handler
        JobFailHandler failHandler = jobRecord.getFailHandler();
        try {
            failHandler.handle();
            handlerSuccess(task);
        } catch (JobExecuteException e) {
            // 此次 planInstance 失败
            planInstanceRepository.end(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), PlanScheduleStatus.FAILED);
            List<PlanInstance> planInstances = planInstanceRepository.list(task.getPlanId(), task.getPlanRecordId());

            PlanRecord planRecord = planRecordRepository.get(task.getPlanId(), task.getPlanRecordId());
            // 判断是否超过重试次数
            if (planRecord.getRetry() >= CollectionUtils.size(planInstances)) {
                eventPublisher.publish(new Event<>(planRecord));
                return;
            }

            // 如果超过重试次数 此planRecord失败
            planRecordRepository.end(task.getPlanId(), task.getPlanRecordId(), PlanScheduleStatus.FAILED);
        }

    }

    /**
     * 判断一批job是否完成可以执行下一步
     */
    public boolean checkJobsFinished(String planId, Long planRecordId, Integer planInstanceId, List<Job> jobs) {
        // 获取db中 job实例
        List<JobRecord> jobRecords = jobRecordRepository.getRecords(planId, planRecordId, planInstanceId,
                jobs.stream().map(Job::getJobId).collect(Collectors.toList()));

        // 有实例还未创建直接返回
        if (jobs.size() > jobRecords.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (JobRecord jobRecord : jobRecords) {
            if (!jobRecord.canTriggerNext()) {
                return false;
            }
        }
        return true;
    }

}
