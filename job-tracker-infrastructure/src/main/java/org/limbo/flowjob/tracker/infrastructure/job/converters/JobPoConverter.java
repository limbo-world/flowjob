package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobDispatchType;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobDO;
import org.limbo.flowjob.tracker.core.job.JobDispatchOption;
import org.limbo.flowjob.tracker.core.job.JobScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.core.job.schedule.JobScheduleCalculator;
import org.limbo.flowjob.tracker.core.job.schedule.JobScheduleCalculatorFactory;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Component
public class JobPoConverter extends Converter<JobDO, JobPO> {

    /**
     * 作业触发计算器工厂
     */
    @Autowired
    private JobScheduleCalculatorFactory jobScheduleCalculatorFactory;

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

        // 先生成一个代理calculator，用于初始化JobDO
        JobScheduleType scheduleType = JobScheduleType.parse(po.getScheduleType());
        DelegatedJobScheduleCalculator delegatedCalculator = new DelegatedJobScheduleCalculator(scheduleType);

        JobDO _do = new JobDO(delegatedCalculator, jobContextRepository);
        _do.setJobId(po.getJobId());
        _do.setJobDesc(po.getJobDesc());

        _do.setDispatchOption(new JobDispatchOption(
                JobDispatchType.parse(po.getDispatchType()),
                po.getCpuRequirement(),
                po.getRamRequirement()
        ));

        _do.setScheduleOption(new JobScheduleOption(
                scheduleType,
                po.getScheduleStartAt(),
                Duration.ofMillis(po.getScheduleDelay()),
                Duration.ofMillis(po.getScheduleInterval()),
                po.getScheduleCron()
        ));

        // 为代理calculator设置真实的计算策略
        delegatedCalculator.delegated = jobScheduleCalculatorFactory.newStrategy(_do);

        return _do;
    }


    /**
     * JobScheduleCalculator代理
     */
    public static class DelegatedJobScheduleCalculator extends JobScheduleCalculator {

        private JobScheduleCalculator delegated;

        protected DelegatedJobScheduleCalculator(JobScheduleType scheduleType) {
            super(scheduleType);
        }

        @Override
        public Boolean canApply(Job job) {
            return delegated.canApply(job);
        }

        @Override
        public Long apply(Job job) {
            return delegated.apply(job);
        }

    }

}
