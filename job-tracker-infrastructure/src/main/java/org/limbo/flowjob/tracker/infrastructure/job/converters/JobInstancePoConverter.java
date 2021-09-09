package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class JobInstancePoConverter extends Converter<JobInstance, JobInstancePO> {

    @Override
    protected JobInstancePO doForward(JobInstance instance) {
        JobInstancePO po = new JobInstancePO();
        po.setPlanId(instance.getPlanId());
        po.setPlanRecordId(instance.getPlanRecordId());
        po.setPlanInstanceId(instance.getPlanInstanceId());
        po.setJobId(instance.getJobId());
        po.setJobInstanceId(instance.getJobInstanceId());
        po.setState(instance.getState().status);
        po.setStartAt(TimeUtil.toLocalDateTime(instance.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(instance.getEndAt()));
        return po;
    }

    @Override
    protected JobInstance doBackward(JobInstancePO po) {
        JobInstance instance = new JobInstance();
        instance.setPlanId(po.getPlanId());
        instance.setPlanRecordId(po.getPlanRecordId());
        instance.setPlanInstanceId(po.getPlanInstanceId());
        instance.setJobId(po.getJobId());
        instance.setJobInstanceId(po.getJobInstanceId());
        instance.setState(JobScheduleStatus.parse(po.getState()));
        instance.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        instance.setEndAt(TimeUtil.toInstant(po.getEndAt()));
        return instance;
    }

}
