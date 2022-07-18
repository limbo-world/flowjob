/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.plan;

import lombok.Data;
import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanSchedulerRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class PlanInstance implements Serializable {

    private static final long serialVersionUID = 1837382860200548371L;

    private String planInstanceId;

    private String planId;

    /**
     * 计划的版本
     */
    private String version;

    /**
     * 计划调度状态
     */
    private PlanScheduleStatus state;

    /**
     * 是否手动下发
     */
    private boolean manual;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    /**
     * 执行图
     */
    private JobDAG dag;

    // ---------------- 需注入
//    @Setter(onMethod_ = @Inject)
//    private transient TrackerNode trackerNode;

    @Setter(onMethod_ = @Inject)
    private transient PlanSchedulerRepository planSchedulerRepo;

    @Setter(onMethod_ = @Inject)
    private transient PlanInstanceRepository planInstanceRepo;

    @Setter(onMethod_ = @Inject)
    private transient JobInstanceRepository jobInstanceRepo;


    /**
     * 检测 Plan 实例是否已经执行完成
     */
    public boolean isAllJobFinished() {
        return checkJobsFinished(dag.jobs());
    }


    /**
     * 判断作业是否可以被触发。检测作业在 DAG 中的前置作业节点是否均可触发子节点。
     */
    public boolean isJobTriggerable(Job job) {
        return checkJobsFinished(dag.getPreJobs(job.getJobId()));
    }


    /**
     * 判断某一计划实例中，一批作业是否全部可以触发下一步
     */
    private boolean checkJobsFinished(List<Job> jobs) {
        // 获取db中 job实例
        Set<String> jobIds = jobs.stream()
                .map(Job::getJobId)
                .collect(Collectors.toSet());
        List<JobInstance> jobInstances = jobInstanceRepo.listInstances(planInstanceId, jobIds);

        // 有实例还未创建直接返回
        if (jobs.size() > jobInstances.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (JobInstance jobInstance : jobInstances) {
            if (!jobInstance.canTriggerNext()) {
                return false;
            }
        }

        return true;
    }


    /**
     * 计划执行成功
     */
    public void executeSucceed() {
        // 更新状态
        setState(PlanScheduleStatus.SUCCEED);
        planInstanceRepo.executeSucceed(this);

        // 检测 Plan 是否需要重新调度，只有 FIXED_INTERVAL 类型的计划，需要在完成时扔到时间轮里重新调度，手动的和其他的都不需要
        PlanScheduler planScheduler = planSchedulerRepo.get(this.version);
        ScheduleType scheduleType = planScheduler.getInfo().getScheduleOption().getScheduleType();
        if (ScheduleType.FIXED_INTERVAL == scheduleType && isManual()) {
            planScheduler.setLastScheduleAt(this.startAt);
            planScheduler.setLastFeedbackAt(TimeUtil.nowInstant());
//            trackerNode.jobTracker().schedule(planScheduler);
        }
    }


    /**
     * 计划执行成功
     */
    public void executeFailed() {
        // 更新状态
        setState(PlanScheduleStatus.FAILED);
        planInstanceRepo.executeFailed(this);
    }

}
