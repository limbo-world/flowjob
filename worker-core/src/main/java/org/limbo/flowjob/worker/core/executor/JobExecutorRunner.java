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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.remote.AbstractRemoteClient;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class JobExecutorRunner {

    private final JobManager jobManager;

    private final JobExecutor executor;

    private final AbstractRemoteClient remoteClient;

    public JobExecutorRunner(JobManager jobManager, JobExecutor executor, AbstractRemoteClient remoteClient) {
        this.jobManager = jobManager;
        this.executor = executor;
        this.remoteClient = remoteClient;
    }

    public void run(Task job) {
        if (jobManager.put(job.getId(), this) != null) {
            // todo 有个相同id的任务在执行
        }
        // 新建线程 执行当前任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                TaskExecuteFeedbackParam dto = new TaskExecuteFeedbackParam();
//                dto.setPlanId(job.getPlanId());
//                dto.setPlanInstanceId(job.getPlanInstanceId());
//                dto.setJobId(job.getJobId());
                try {
                    dto.setParams(executor.run(job));
                    dto.setResult(ExecuteResult.SUCCEED);
                } catch (Exception e) {
                    log.error("job run error", e);
                    dto.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
                    dto.setResult(ExecuteResult.FAILED);
                } finally {
                    // 返回结果 todo 超时等情况，需要重试
                    remoteClient.taskExecuted(dto);
                    // 最终都要移除任务
                    jobManager.remove(job.getId());
                }
            }
        }).start();
    }

}
