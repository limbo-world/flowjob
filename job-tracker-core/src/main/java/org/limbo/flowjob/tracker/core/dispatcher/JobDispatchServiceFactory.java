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

package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.core.job.context.JobContext;

/**
 * 作业执行器工厂类
 *
 * @author Brozen
 * @since 2021-05-19
 */
public interface JobDispatchServiceFactory {

    /**
     * 作业执行器工厂方法，根据上下文生成一个新的作业执行器。
     * @param context 作业上下文
     * @return 作业执行器
     */
    JobDispatchService newDispatchService(JobContext context);

}
