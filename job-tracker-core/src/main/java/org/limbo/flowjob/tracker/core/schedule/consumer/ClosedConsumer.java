package org.limbo.flowjob.tracker.core.schedule.consumer;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.JobNodeType;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.handler.JobFailHandler;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class ClosedConsumer implements Consumer<JobInstance> {

    private final JobInstanceRepository jobInstanceRepository;

    private final PlanInstanceRepository planInstanceRepository;

    private final PlanRepository planRepository;

    private final TrackerNode trackerNode;

    private final JobInstanceStorage jobInstanceStorage;

    public ClosedConsumer(JobInstanceRepository jobInstanceRepository,
                          PlanInstanceRepository planInstanceRepository,
                          PlanRepository planRepository,
                          TrackerNode trackerNode,
                          JobInstanceStorage jobInstanceStorage) {
        this.jobInstanceRepository = jobInstanceRepository;
        this.planInstanceRepository = planInstanceRepository;
        this.planRepository = planRepository;
        this.trackerNode = trackerNode;
        this.jobInstanceStorage = jobInstanceStorage;
    }

    @Override
    public void accept(JobInstance jobInstance) {
        // 更新 job instance 状态
        if (log.isDebugEnabled()) {
            log.debug(jobInstance.getWorkerId() + " closed " + jobInstance.getId());
        }
        jobInstanceRepository.updateInstance(jobInstance);

        // 如果plan已经是结束的（某个节点异常导致plan失败）
        PlanInstance planInstance = planInstanceRepository.getInstance(jobInstance.getPlanId(), jobInstance.getPlanInstanceId());
        if (PlanScheduleStatus.SUCCEED == planInstance.getState() || PlanScheduleStatus.FAILED == planInstance.getState()) {
            return;
        }

        switch (jobInstance.getState()) {
            case SUCCEED:
                handlerSuccess(planInstance, jobInstance);
                break;

            case FAILED:
                handlerFailed(planInstance, jobInstance);
                break;
            // todo 手动关闭是否要单独处理
        }
    }

    public void handlerSuccess(PlanInstance planInstance, JobInstance jobInstance) {
        Plan plan = planRepository.getPlan(planInstance.getPlanId(), planInstance.getVersion());

        // 判断是否有后续节点，后续job是否能执行
        List<Job> subJobs = plan.getSubJobs(jobInstance.getJobId());
        for (Job job : subJobs) {
            // 判断是否满足下发条件
            if (!preCheckBeforeJobCreate(plan.getPlanId(), planInstance.getPlanInstanceId(), plan.getPreJobs(job.getJobId()))) {
                continue;
            }

            if (JobNodeType.END == job.getNodeType()) {
                // 如果是end节点 且end节点已经满足结束情况
                planInstanceRepository.endInstance(planInstance.getPlanId(), planInstance.getPlanInstanceId(), PlanScheduleStatus.SUCCEED);

                // 判断 plan 是否需要 feedback 只有 FIXED_INTERVAL类型需要反馈，让任务在时间轮里面能重新下发，手动的和其他的都不需要
                plan.setLastScheduleAt(planInstance.getStartAt());
                plan.setLastFeedBackAt(planInstance.getEndAt());
                if (ScheduleType.FIXED_INTERVAL == plan.getScheduleOption().getScheduleType() && planInstance.isReschedule()) {
                    trackerNode.jobTracker().schedule(plan);
                }
            } else {
                // 不为end节点，继续下发
                jobInstanceStorage.store(job.newInstance(plan.getPlanId(), planInstance.getPlanInstanceId(), plan.getVersion(), JobScheduleStatus.Scheduling));
            }
        }

    }

    public void handlerFailed(PlanInstance planInstance, JobInstance jobInstance) {
        JobFailHandler failHandler = jobInstance.getFailHandler();

        // 执行处理器操作
        failHandler.execute();

        if (failHandler.terminate()) {
            // todo 判断是否超过重试 超过则结束plan 否则重试
            // todo 重试应该考虑 是否worker能跑同参数的相同任务

            planInstanceRepository.endInstance(planInstance.getPlanId(), planInstance.getPlanInstanceId(), PlanScheduleStatus.SUCCEED);
        } else {
            handlerSuccess(planInstance, jobInstance);
        }

    }

    public boolean preCheckBeforeJobCreate(String planId, Long planInstanceId, List<Job> preJobs) {
        // 获取db中 job实例
        List<JobInstance> preInstances = jobInstanceRepository.getInstances(planId, planInstanceId,
                preJobs.stream().map(Job::getJobId).collect(Collectors.toList()));
        // 有实例还未创建直接返回
        if (preJobs.size() > preInstances.size()) {
            return false;
        }
        // 判断是否所有实例都可以触发下个任务
        for (JobInstance preInstance : preInstances) {
            if (!preInstance.canTriggerNext()) {
                return false;
            }
        }
        return true;
    }

}
