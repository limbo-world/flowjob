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

package org.limbo.flowjob.broker.core.plan.job.context;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.exceptions.JobExecuteException;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;

/**
 * 支持重试的任务，支持：
 * <ul>
 *     <li> 下发失败重试 </li>
 *     <li> 执行失败重试 </li>
 * </ul>
 *
 * @author Brozen
 * @since 2022-06-28
 */
@Slf4j
public class RetriableTask extends Task {


    /**
     * {@inheritDoc} 并触发作业中配置的重试策略。
     *
     * @param scheduler 可调度作业信息
     * @param planInstance 执行计划实例
     * @param jobInstance 作业实例
     * @param errorMsg 执行失败的异常信息
     * @param errorStackTrace 执行失败的异常堆栈
     */
    @Override
    public void failed(PlanScheduler scheduler, PlanInstance planInstance, JobInstance jobInstance, String errorMsg, String errorStackTrace) {
        // 执行父方法，更新状态
        super.failed(scheduler, planInstance, jobInstance, errorMsg, errorStackTrace);

        // 判断重试到第几次，是否超过最大重试次数
        Integer maxRetryTimes = jobInstance.getRetry();
        Integer retriedTimes = 0; // TODO 怎么取？

        // 未达到最大重试次数，尝试重新下发
        if (retriedTimes >= maxRetryTimes) {
            // TODO 重新下发
            return;
        }


        // 否则则认为作业执行失败，更新作业状态
        jobInstance.failed();

        // 超过重试次数 执行作业失败 handler
        JobFailHandler failHandler = jobInstance.getFailHandler();
        // Job执行失败时，是否需要终止 Plan
        boolean terminatePlan = true;
        try {
            if (failHandler != null) {
                failHandler.handle();
                terminatePlan = failHandler.terminate();
            }
        } catch (JobExecuteException e) {
            log.error("作业执行失败处理器发生异常", e);
        }

        // 终止 Plan 或继续下发任务
        if (terminatePlan) {
            planInstance.executeFailed();
        } else {
            dispatchNextTask(scheduler, planInstance);
        }


    }
}
