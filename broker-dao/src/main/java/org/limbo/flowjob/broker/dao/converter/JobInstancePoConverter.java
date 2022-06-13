package org.limbo.flowjob.broker.dao.converter;

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.entity.JobInstanceContextEntity;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class JobInstancePoConverter extends Converter<JobInstance, JobInstanceContextEntity> {

    @Override
    protected JobInstanceContextEntity doForward(JobInstance instance) {
        JobInstanceContextEntity po = new JobInstanceContextEntity();
        JobInstance.ID instanceId = instance.getId();
        po.setPlanId(instanceId.planId);
        po.setPlanRecordId(instanceId.planRecordId);
        po.setPlanInstanceId(instanceId.planInstanceId);
        po.setJobId(instanceId.jobId);
        po.setJobInstanceId(instanceId.jobInstanceId);
        po.setState(instance.getState().status);
        po.setStartAt(TimeUtil.toLocalDateTime(instance.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(instance.getEndAt()));
        return po;
    }

    @Override
    protected JobInstance doBackward(JobInstanceContextEntity po) {
        JobInstance instance = new JobInstance();
        JobInstance.ID instanceId = new JobInstance.ID(
                po.getPlanId(),
                po.getPlanRecordId(),
                po.getPlanInstanceId(),
                po.getJobId(),
                po.getJobInstanceId()
        );
        instance.setId(instanceId);
        instance.setState(JobScheduleStatus.parse(po.getState()));
        instance.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        instance.setEndAt(TimeUtil.toInstant(po.getEndAt()));
        return instance;
    }

}
