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
import org.limbo.flowjob.tracker.commons.constants.enums.ExecuteResult;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecuteFeedbackDto;
import org.limbo.flowjob.tracker.commons.utils.Symbol;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.job.context.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * @author Brozen
 * @since 2021-07-07
 */
@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EventPublisher<Event<?>> eventPublisher;

    /**
     * 处理任务执行反馈
     *
     * @param mono 反馈参数
     */
    public Mono<Symbol> feedback(Mono<JobExecuteFeedbackDto> mono) {
        return mono.transformDeferredContextual((feedbackMono, ctx) -> feedbackMono.map(fb -> {
            // 获取实例
            Task task = taskRepository.get(fb.getPlanId(), fb.getPlanRecordId(), fb.getPlanInstanceId(),
                    fb.getJobId(), fb.getJobInstanceId(), fb.getTaskId());
            ExecuteResult result = fb.getResult();

            // 订阅
            task.onClosed().subscribe(new Consumer<Task>() {
                @Override
                public void accept(Task task) {
                    if (log.isDebugEnabled()) {
                        log.debug(task.getWorkerId() + " closed " + task.getId());
                    }

                    // 前置校验
                    if (task.isNeedPublish()) {
                        taskRepository.executed(task);
                        // 修改状态，先返回成功给worker 让worker继续执行 将task丢到队列异步修改后续状态
                        eventPublisher.publish(new Event<>(task));
                    }
                }
            });

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
        }));
    }


}
