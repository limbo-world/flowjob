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

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface JobContextRepository {

    /**
     * 获取作业执行的上下文
     * @param jobId 作业ID
     * @param contextId 上下文ID
     * @return 作业上下文
     */
    default JobContext getContext(String jobId, String contextId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取最近一次作业执行时的上下文
     * @param jobId 作业ID
     * @return 最近一次作业执行时的上下文
     */
    default JobContext getLatestContext(String jobId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 持久化作业上下文
     * @param context 作业执行上下文
     */
    default void addContext(JobContext context) {
        throw new UnsupportedOperationException();
    }

    /**
     * 更新作业上下文
     * @param context 作业执行上下文
     */
    default void updateContext(JobContext context) {
        throw new UnsupportedOperationException();
    }
}
