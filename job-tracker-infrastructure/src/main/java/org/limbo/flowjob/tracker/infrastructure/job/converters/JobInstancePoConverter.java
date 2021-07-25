package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;
import org.springframework.stereotype.Component;

/**
 * todo
 *
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class JobInstancePoConverter extends Converter<JobInstance, JobInstancePO> {

    @Override
    protected JobInstancePO doForward(JobInstance instance) {
        return null;
    }

    @Override
    protected JobInstance doBackward(JobInstancePO jobInstancePO) {
        return null;
    }

}
