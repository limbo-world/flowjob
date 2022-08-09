/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.JobInstanceConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class JobInstanceRepo implements JobInstanceRepository {

    @Inject
    private JobInstanceConverter convert;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    // todo 移除
    @Setter(onMethod_ = @Inject)
    private TaskRepo taskRepo;

    @Override
    public String save(JobInstance jobInstance) {
        JobInstanceEntity entity = this.convert.convert(jobInstance);
        jobInstanceEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }


    @Override
    public JobInstance get(String jobInstanceId) {
        return jobInstanceEntityRepo.findById(Long.valueOf(jobInstanceId)).map(entity -> convert.reverse().convert(entity)).orElse(null);
    }

}
