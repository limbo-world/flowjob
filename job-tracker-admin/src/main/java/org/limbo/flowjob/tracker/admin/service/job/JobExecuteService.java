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

package org.limbo.flowjob.tracker.admin.service.job;

import org.limbo.flowjob.tracker.commons.constants.WorkerHeaders;
import org.limbo.flowjob.tracker.commons.constants.enums.JobExecuteResult;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecuteFeedbackDto;
import org.limbo.flowjob.tracker.commons.utils.Symbol;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-07-07
 */
@Service
public class JobExecuteService {

    @Autowired
    private JobInstanceRepository jobInstanceRepository;



    /**
     * 处理任务执行反馈
     * @param mono 反馈参数
     */
    public Mono<Symbol> feedback(Mono<JobExecuteFeedbackDto> mono) {
        return mono.transformDeferredContextual((feedbackMono, ctx) -> {

            // 从context读取workerId
            String workerId = ctx.getOrDefault(WorkerHeaders.WORKER_ID, "");

            return feedbackMono.map(fb -> {

                // 更新context状态
                JobInstance context = jobInstanceRepository.getInstance(fb.getJobId(), fb.getContextId());
                JobExecuteResult result = fb.getResult();
                switch (result) {
                    case SUCCEED:
                        context.closeContext();
                        break;

                    case FAILED:
                        context.closeContext(fb.getErrorMsg(), fb.getErrorStackTrace());
                        break;

                    case TERMINATED:
                        throw new UnsupportedOperationException("暂不支持手动终止任务");
                }

                // TODO 触发任务完成事件，进行任务再次调度检测

                return Symbol.newSymbol();
            });

        });
    }



}
