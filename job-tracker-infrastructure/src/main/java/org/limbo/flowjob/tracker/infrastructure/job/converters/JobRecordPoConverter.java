package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.context.JobRecord;
import org.limbo.flowjob.tracker.dao.po.JobRecordPO;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class JobRecordPoConverter extends Converter<JobRecord, JobRecordPO> {

    @Override
    protected JobRecordPO doForward(JobRecord record) {
        JobRecordPO po = new JobRecordPO();
        JobRecord.ID recordId = record.getId();
        po.setPlanId(recordId.planId);
        po.setPlanRecordId(recordId.planRecordId);
        po.setPlanInstanceId(recordId.planInstanceId);
        po.setJobId(recordId.jobId);
        po.setState(record.getState().status);
        po.setAttributes(record.getAttributes());
        po.setStartAt(TimeUtil.toLocalDateTime(record.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(record.getEndAt()));
        return po;
    }

    @Override
    protected JobRecord doBackward(JobRecordPO po) {
        JobRecord record = new JobRecord();
        JobRecord.ID recordId = new JobRecord.ID(
                po.getPlanId(),
                po.getPlanRecordId(),
                po.getPlanInstanceId(),
                po.getJobId()
        );
        record.setId(recordId);
        record.setState(JobScheduleStatus.parse(po.getState()));
        record.setAttributes(record.getAttributes());
        record.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        record.setEndAt(TimeUtil.toInstant(po.getEndAt()));
        return record;
    }

}
