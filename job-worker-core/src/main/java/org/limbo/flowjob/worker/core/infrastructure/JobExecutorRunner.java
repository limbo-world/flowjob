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

package org.limbo.flowjob.worker.core.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobExecuteResult;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecuteFeedbackDto;
import org.limbo.flowjob.worker.core.domain.Job;

/**
 * @author Devil
 * @date 2021/6/24 4:14 下午
 */
@Slf4j
public class JobExecutorRunner {

    private final JobManager jobManager;

    private final JobExecutor executor;

    public JobExecutorRunner(JobManager jobManager, JobExecutor executor) {
        this.jobManager = jobManager;
        this.executor = executor;
    }

    public void run(Job job) {
        if (jobManager.put(job.getId(), this) != null) {
            // todo 有个相同id的任务在执行
        }
        // 新建线程 执行当前任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                JobExecuteFeedbackDto dto = new JobExecuteFeedbackDto();
                dto.setJobId(job.getId());
                try {
                    dto.setParams(executor.run(job));
                    dto.setResult(JobExecuteResult.SUCCEED);
                } catch (Exception e) {
                    log.error("job run error", e);
                    dto.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
                    dto.setResult(JobExecuteResult.FAILED);
                } finally {
                    AbstractRemoteClient client = RemoteClientCenter.getClient();
                    client.jobExecuted(dto);
                    // 最终都要移除任务
                    jobManager.remove(job.getId());
                }
            }
        }).start();
    }

}
