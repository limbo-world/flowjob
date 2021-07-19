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

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface JobInstanceRepository {

    /**
     * 持久化作业实例
     * @param instance 作业执行实例
     */
    default void addInstance(JobInstance instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * TODO
     * 更新作业实例
     * @param instance 作业执行实例
     */
    default void updateInstance(JobInstance instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取作业执行实例
     * @param jobId 作业ID
     * @param jobInstanceId 实例ID
     * @return 作业实例
     */
    default JobInstance getInstance(String jobId, String jobInstanceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取最近一次作业执行时的实例
     * @param jobId 作业ID
     * @return 最近一次作业执行时的实例
     */
    default JobInstance getLatestInstance(String jobId) {
        throw new UnsupportedOperationException();
    }
}
