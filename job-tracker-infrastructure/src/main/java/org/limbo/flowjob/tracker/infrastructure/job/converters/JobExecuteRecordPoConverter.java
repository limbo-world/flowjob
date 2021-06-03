package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.core.job.context.JobAttributes;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.dao.po.JobExecuteRecordPO;
import org.limbo.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Component
public class JobExecuteRecordPoConverter extends Converter<JobContext, JobExecuteRecordPO> {

    /**
     * 上下文repo
     */
    @Autowired
    private JobContextRepository jobContextRepository;

    /**
     * 将{@link JobContext}转换为{@link JobExecuteRecordPO}
     * @param _do {@link JobContext}领域对象
     * @return {@link JobExecuteRecordPO}持久化对象
     */
    @Override
    @NotNull
    protected JobExecuteRecordPO doForward(JobContext _do) {

        JobExecuteRecordPO po = new JobExecuteRecordPO();
        po.setJobId(_do.getJobId());
        po.setRecordId(_do.getContextId());
        po.setStatus(_do.getStatus().status);
        po.setWorkerId(_do.getWorkerId());
        po.setAttributes(JacksonUtils.toJSONString(_do.getJobAttributes()));

        return po;

    }

    /**
     *
     * 将{@link JobExecuteRecordPO}转换为{@link JobContext}
     * @param po {@link JobExecuteRecordPO}持久化对象
     * @return {@link JobContext}领域对象
     */
    @Override
    protected JobContext doBackward(JobExecuteRecordPO po) {

        JobContext _do = new JobContext(jobContextRepository);
        _do.setJobId(po.getJobId());
        _do.setContextId(po.getRecordId());
        _do.setStatus(JobContextStatus.parse(po.getStatus()));
        _do.setWorkerId(po.getWorkerId());
        _do.setJobAttributes(JacksonUtils.parseObject(po.getAttributes(), JobAttributes.class));

        return _do;

    }

}
