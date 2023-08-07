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

package org.limbo.flowjob.agent.repository;

import org.limbo.flowjob.agent.Job;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Worker 中执行的任务仓库
 *
 * @author Devil
 * @since 2021/7/24
 */
public class JobRepository {


    private final Map<String, Job> map = new ConcurrentHashMap<>();


    /**
     * 尝试新增任务到仓库中：如果已存在相同 taskId 的任务，则不添加新的任务，返回 false；如不存在，则添加成功，返回 true。
     *
     * @param job 任务
     */
    public boolean save(Job job) {
        return map.putIfAbsent(job.getId(), job) == null;
    }


    /**
     * 从仓库中移除任务
     */
    public void delete(String id) {
        map.remove(id);
    }


    /**
     * 统计运行中的任务数量
     */
    public int count() {
        return map.size();
    }


    /**
     * 判断在仓库中是否存在指定 ID 的任务
     */
    public boolean has(String id) {
        return map.containsKey(id);
    }

}
