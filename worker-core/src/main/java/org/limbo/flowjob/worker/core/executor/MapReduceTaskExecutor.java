/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.executor;


/**
 * 任务执行器
 *
 * @author Devil
 * @since 2021/7/24
 */
public interface MapReduceTaskExecutor {

    /**
     * 切分创建多个子task
     * @param context 任务执行上下文
     */
    void split(ExecuteContext context);

    /**
     * 处理map分片任务
     * @param context 任务执行上下文
     */
    void map(ExecuteContext context);

    /**
     * 处理reduce任务
     * @param context 任务执行上下文
     */
    void reduce(ExecuteContext context);


    /**
     * 执行器名称，默认为执行器类的类名
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }


    /**
     * 执行器描述，默认为执行器类的类全名
     */
    default String getDescription() {
        return this.getClass().getName();
    }

}