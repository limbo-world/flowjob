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

package org.limbo.flowjob.tracker.core.job.context;

import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface JobInstanceRepository {

    void executing(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId);

    void end(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, JobScheduleStatus state);


    Integer createId(String planId, Long planRecordId, Integer planInstanceId, String jobId);

    /**
     * 持久化作业实例
     * @param instance 作业执行实例
     */
    void add(JobInstance instance);

    /**
     * 列出 JobRecord 下对应的所有 JobInstance
     * @param planId
     * @param planRecordId
     * @param planInstanceId
     * @param jobId
     * @return
     */
    List<JobInstance> listByRecord(String planId, Long planRecordId, Integer planInstanceId, String jobId);
}
