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


    JobInstance.ID createId(JobRecord.ID jobRecordId);


    /**
     * 持久化作业实例
     * @param instance 作业执行实例
     */
    void add(JobInstance instance);


    void end(JobInstance.ID jobInstanceId, JobScheduleStatus state);


    /**
     * CAS 将此作业执行实例状态从 {@link JobScheduleStatus#SCHEDULING} 修改为 {@link JobScheduleStatus#EXECUTING}
     * @param jobInstanceId 待更新的作业执行实例ID
     * @return 更新是否成功
     */
    boolean execute(JobInstance.ID jobInstanceId);


    /**
     * 根据ID查询作业执行实例
     * @param jobInstanceId 作业执行实例ID
     * @return 作业执行实例
     */
    JobInstance get(JobInstance.ID jobInstanceId);


    /**
     * 列出 JobRecord 下对应的所有 JobInstance
     * @param jobRecordId 作业执行记录ID
     * @return 作业执行记录下关联的所有作业实例
     */
    List<JobInstance> listByRecord(JobRecord.ID jobRecordId);
}
