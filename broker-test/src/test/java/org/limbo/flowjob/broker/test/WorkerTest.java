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
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.utils.json.JacksonUtils;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerExecutorEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerExecutorEntityRepo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Devil
 * @since 2022/6/24
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class WorkerTest {

    @Setter(onMethod_ = @Inject)
    private WorkerExecutorEntityRepo workerExecutorEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerEntityRepo workerEntityRepo;

    @Test
    public void findExecutor() {
        List<WorkerExecutorEntity> workerExecutorEntities = workerExecutorEntityRepo.findByWorkerId("12B39DF31A46455581C20ED79968E867");
        System.out.println(JacksonUtils.toJSONString(workerExecutorEntities));
    }

    @Test
    public void findWorker() {
        List<WorkerEntity> byStatusAndDeleted = workerEntityRepo.findByStatusAndDeleted(WorkerStatus.RUNNING.status, Boolean.FALSE);
        for (WorkerEntity workerEntity : byStatusAndDeleted) {
            System.out.println(workerEntity.getId() + " " + workerEntity.getStatus() + workerEntity.getDeleted());
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    public void delete() {
        int d = workerExecutorEntityRepo.deleteByWorkerId("12B39DF31A46455581C20ED79968E867");
        System.out.println(d);
    }

}
