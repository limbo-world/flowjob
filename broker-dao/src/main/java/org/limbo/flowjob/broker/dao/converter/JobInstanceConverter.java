package org.limbo.flowjob.broker.dao.converter;

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class JobInstanceConverter extends Converter<JobInstance, JobInstanceEntity> {

    @Override
    protected JobInstanceEntity doForward(JobInstance record) {
        // todo
        return null;
//        JobInstanceEntity po = new JobInstanceEntity();
//        JobInstance.ID recordId = record.getId();
//        po.setPlanId(recordId.planId);
//        po.setPlanRecordId(recordId.planRecordId);
//        po.setPlanInstanceId(recordId.planInstanceId);
//        po.setJobId(recordId.jobId);
//        po.setState(record.getState().status);
//        po.setAttributes(record.getAttributes());
//        po.setStartAt(TimeUtil.toLocalDateTime(record.getStartAt()));
//        po.setEndAt(TimeUtil.toLocalDateTime(record.getEndAt()));
//        return po;
    }

    @Override
    protected JobInstance doBackward(JobInstanceEntity po) {
        // todo
        return null;
//        JobInstance record = new JobInstance();
//        JobInstance.ID recordId = new JobInstance.ID(
//                po.getPlanId(),
//                po.getPlanRecordId(),
//                po.getPlanInstanceId(),
//                po.getJobId()
//        );
//        record.setId(recordId);
//        record.setState(JobScheduleStatus.parse(po.getState()));
//        record.setAttributes(record.getAttributes());
//        record.setStartAt(TimeUtil.toInstant(po.getStartAt()));
//        record.setEndAt(TimeUtil.toInstant(po.getEndAt()));
//        return record;
    }

}
