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

package org.limbo.flowjob.tracker.core.job;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface JobRepository {

    /**
     * 批量保存
     * @param jobs 作业数据
     */
    void batchInsert(List<Job> jobs);

    /**
     * 根据 planId 获取未删除的job
     * @param planId
     * @return
     */
    List<Job> getUsedJobsByPlan(String planId);

    /**
     * 根据 planId 删除未删除的job
     * @param planId
     */
    void deleteUsedJobsByPlan(String planId);

    /**
     * 根据id查询作业
     * @param jobId jobId
     * @return 作业
     */
    Job getJob(String jobId);

}
