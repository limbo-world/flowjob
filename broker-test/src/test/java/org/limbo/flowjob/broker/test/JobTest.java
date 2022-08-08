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

package org.limbo.flowjob.broker.test;

import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2022/8/3
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class JobTest {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Test
    public void insert() {
        List<JobInstanceEntity> jobInstances = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            JobInstanceEntity jobInstance = new JobInstanceEntity();


            jobInstances.add(jobInstance);
        }

        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.saveAll(jobInstances);

        System.out.println(JacksonUtils.toJSONString(jobInstances));
        System.out.println(JacksonUtils.toJSONString(jobInstanceEntities));
    }

}
