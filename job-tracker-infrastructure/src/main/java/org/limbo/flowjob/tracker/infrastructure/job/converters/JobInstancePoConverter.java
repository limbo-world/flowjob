package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class JobInstancePoConverter extends Converter<JobInstance, JobInstancePO> {

    @Autowired
    private PlanRepository planRepository;

    @Override
    protected JobInstancePO doForward(JobInstance instance) {
        JobInstancePO po = new JobInstancePO();
        po.setPlanId(instance.getPlanId());
        po.setPlanInstanceId(instance.getPlanInstanceId());
        po.setJobId(instance.getJobId());
        po.setVersion(instance.getVersion());
        po.setState(instance.getState().status);
        return po;
    }

    @Override
    protected JobInstance doBackward(JobInstancePO po) {
        Plan plan = planRepository.getPlan(po.getPlanId(), po.getVersion());
        Job job = plan.getDag().getJob(po.getJobId());
        return job.newInstance(po.getPlanId(), po.getPlanInstanceId(), po.getVersion(), JobScheduleStatus.parse(po.getState()));
    }

}
