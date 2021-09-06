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

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.WorkerHeaders;
import org.limbo.flowjob.tracker.commons.constants.enums.ExecuteResult;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecuteFeedbackDto;
import org.limbo.flowjob.tracker.commons.utils.Symbol;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.job.context.TaskRepository;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.job.consumer.ClosedConsumer;
import org.limbo.flowjob.tracker.core.storage.Storage;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-07-07
 */
@Slf4j
@Service
public class JobExecuteService {

    @Autowired
    private TrackerNode trackerNode;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PlanInstanceRepository planInstanceRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private Storage storage;

    /**
     * 处理任务执行反馈
     *
     * @param mono 反馈参数
     */
    public Mono<Symbol> feedback(Mono<JobExecuteFeedbackDto> mono) {
        return mono.transformDeferredContextual((feedbackMono, ctx) -> {

            // todo 从context读取workerId ??? 不知道这行干嘛
            String workerId = ctx.getOrDefault(WorkerHeaders.WORKER_ID, "");

            return feedbackMono.map(fb -> {

                // 获取实例
                Task task = taskRepository.get(fb.getPlanId(), fb.getPlanRecordId(), fb.getPlanInstanceId(),
                        fb.getJobId(), fb.getJobInstanceId(), fb.getTaskId());
                ExecuteResult result = fb.getResult();

                // 订阅执行情况
                task.onClosed().subscribe(new ClosedConsumer(taskRepository, planInstanceRepository,
                        planRepository, trackerNode, storage));

                // 变更状态
                switch (result) {
                    case SUCCEED:
                        task.close();
                        break;

                    case FAILED:
                        task.close(fb.getErrorMsg(), fb.getErrorStackTrace());
                        break;

                    case TERMINATED:
                        throw new UnsupportedOperationException("暂不支持手动终止任务");
                }

                return Symbol.newSymbol();
            });

        });
    }


}
