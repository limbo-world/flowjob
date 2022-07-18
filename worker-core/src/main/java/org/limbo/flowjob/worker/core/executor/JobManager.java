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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * job 中心
 * @author Devil
 * @since 2021/7/24
 */
public class JobManager {

    private final Map<String, JobExecutorRunner> jobs = new ConcurrentHashMap<>();

    public JobExecutorRunner put(String id, JobExecutorRunner runner) {
        return jobs.putIfAbsent(id, runner);
    }

    public void remove(String id) {
        jobs.remove(id);
    }

    /**
     * 运行中的任务数
     */
    public int size() {
        return jobs.size();
    }

    public boolean hasJob(String id) {
        return jobs.containsKey(id);
    }

}
