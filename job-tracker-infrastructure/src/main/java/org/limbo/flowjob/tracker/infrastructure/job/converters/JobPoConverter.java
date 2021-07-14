/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.dao.po.JobPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Component
public class JobPoConverter extends Converter<Job, JobPO> {

    /**
     * 上下文repository
     */
    @Autowired
    private JobContextRepository jobContextRepository;

    /**
     * 将{@link Job}转换为{@link JobPO}
     * @param _do JobDO领域对象
     * @return JobPO持久化对象
     */
    @Override
    protected JobPO doForward(Job _do) {
        JobPO po = new JobPO();
        po.setPlanId(_do.getPlanId());
        po.setJobId(_do.getJobId());
        po.setJobDesc(_do.getJobDesc());

        DispatchOption dispatchOption = _do.getDispatchOption();
        po.setDispatchType(dispatchOption.getDispatchType().type);
        po.setCpuRequirement(dispatchOption.getCpuRequirement());
        po.setRamRequirement(dispatchOption.getRamRequirement());

        return po;
    }

    /**
     * 将{@link JobPO}转换为{@link Job}
     * @param po JobPO持久化对象
     * @return JobDO领域对象
     */
    @Override
    protected Job doBackward(JobPO po) {
        Job _do = new Job(jobContextRepository);
        _do.setPlanId(po.getPlanId());
        _do.setJobId(po.getJobId());
        _do.setJobDesc(po.getJobDesc());
        _do.setParentJobIds(StringUtils.isBlank(po.getParentJobIds())
                ? Lists.newArrayList()
                : Arrays.asList(StringUtils.split(po.getParentJobIds(), ",")));

        _do.setDispatchOption(new DispatchOption(
                DispatchType.parse(po.getDispatchType()),
                po.getCpuRequirement(),
                po.getRamRequirement()
        ));

        return _do;
    }


}
