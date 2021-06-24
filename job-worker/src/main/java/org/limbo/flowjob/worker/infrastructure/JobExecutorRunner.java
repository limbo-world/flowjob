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

package org.limbo.flowjob.worker.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.worker.domain.Job;
import org.limbo.flowjob.worker.domain.Worker;

/**
 * @author Devil
 * @date 2021/6/24 4:14 下午
 */
@Slf4j
public class JobExecutorRunner {

    private final JobRunCenter jobRunCenter;

    private final JobExecutor executor;

    public JobExecutorRunner(JobRunCenter jobRunCenter, JobExecutor executor) {
        this.jobRunCenter = jobRunCenter;
        this.executor = executor;
    }

    public void run(Job job) {
        if (jobRunCenter.put(job.getId(), this) != null) {
            // todo 有个相同id的任务在执行
        }
        // 新建线程 执行当前任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    executor.run(job);
                    // todo 发送任务成功信息 x
                } catch (Exception e) {
                    log.error("job run error", e);
                    // todo 发送任务失败信息
                } finally {
                    // 最终都要移除任务
                    jobRunCenter.remove(job.getId());
                }
            }
        }).start();
    }

}
