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

package org.limbo.flowjob.tracker.core;

import java.util.List;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface JobContext {


    /**
     * 获取作业。
     * @return 当前执行中的作业。
     */
    Job getJob();


    /**
     * 获取作业属性。
     * @return {@link JobAttributes}
     */
    JobAttributes getJobAttributes();


    /**
     * 获取执行此作业的worker
     * @return 执行此作业的worker
     */
    Worker getWorker();

}
