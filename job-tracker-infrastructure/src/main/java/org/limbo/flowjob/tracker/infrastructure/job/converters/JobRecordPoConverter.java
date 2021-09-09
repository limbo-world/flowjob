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
        po.setPlanId(record.getPlanId());
        po.setPlanRecordId(record.getPlanRecordId());
        po.setPlanInstanceId(record.getPlanInstanceId());
        po.setJobId(record.getJobId());
        po.setState(record.getState().status);
        po.setAttributes(record.getAttributes());
        po.setStartAt(TimeUtil.toLocalDateTime(record.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(record.getEndAt()));
        return po;
    }

    @Override
    protected JobRecord doBackward(JobRecordPO po) {
        JobRecord record = new JobRecord();
        record.setPlanId(po.getPlanId());
        record.setPlanRecordId(po.getPlanRecordId());
        record.setPlanInstanceId(po.getPlanInstanceId());
        record.setJobId(po.getJobId());
        record.setState(JobScheduleStatus.parse(po.getState()));
        record.setAttributes(record.getAttributes());
        record.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        record.setEndAt(TimeUtil.toInstant(po.getEndAt()));
        return record;
    }

}
