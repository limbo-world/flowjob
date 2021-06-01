package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobDispatchType;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;
import org.limbo.flowjob.tracker.core.job.JobDO;
import org.limbo.flowjob.tracker.core.job.JobDispatchOption;
import org.limbo.flowjob.tracker.core.job.JobScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.core.job.schedule.JobScheduleCalculator;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Component
public class JobPoDoConverter extends Converter<JobDO, JobPO> {

    /**
     * 作业触发计算器
     */
    @Autowired
    private JobScheduleCalculator triggerCalculator;

    /**
     * 上下文repository
     */
    @Autowired
    private JobContextRepository jobContextRepository;

    /**
     * 将{@link JobDO}转换为{@link JobPO}
     * @param _do JobDO领域对象
     * @return JobPO持久化对象
     */
    @Override
    protected JobPO doForward(JobDO _do) {
        JobPO po = new JobPO();
        po.setJobId(_do.getJobId());
        po.setJobDesc(_do.getJobDesc());

        JobDispatchOption dispatchOption = _do.getDispatchOption();
        po.setDispatchType(dispatchOption.getDispatchType().type);
        po.setCpuRequirement(dispatchOption.getCpuRequirement());
        po.setRamRequirement(dispatchOption.getRamRequirement());

        JobScheduleOption scheduleOption = _do.getScheduleOption();
        po.setScheduleType(scheduleOption.getScheduleType().type);
        po.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        po.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        po.setScheduleCron(scheduleOption.getScheduleCron());

        return po;
    }

    /**
     * 将{@link JobPO}转换为{@link JobDO}
     * @param po JobPO持久化对象
     * @return JobDO领域对象
     */
    @Override
    protected JobDO doBackward(JobPO po) {
        JobDO _do = new JobDO(triggerCalculator, jobContextRepository);
        _do.setJobId(po.getJobId());
        _do.setJobDesc(po.getJobDesc());

        _do.setDispatchOption(new JobDispatchOption(
                JobDispatchType.parse(po.getDispatchType()),
                po.getCpuRequirement(),
                po.getRamRequirement()
        ));

        _do.setScheduleOption(new JobScheduleOption(
                JobScheduleType.parse(po.getScheduleType()),
                Duration.ofMillis(po.getScheduleDelay()),
                Duration.ofMillis(po.getScheduleInterval()),
                po.getScheduleCron()
        ));

        return _do;
    }

}
