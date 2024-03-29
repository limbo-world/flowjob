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

package org.limbo.flowjob.broker.core.meta.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.processor.InstanceProcessor;
import org.limbo.flowjob.broker.core.meta.processor.InstanceProcessorFactory;
import org.limbo.flowjob.common.constants.JobConstant;
import org.limbo.flowjob.common.thread.CommonThreadPool;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.LocalDateTimeUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * job 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致job没有完成的原因
 * 1. agent服务真实下线
 * 2. agent服务假死
 * 3. agent完成job调用broker的接口失败
 */
@Slf4j
public class JobExecuteCheckTask {

    private final JobInstanceRepository jobInstanceRepository;

    private final Broker broker;

    private final NodeManger nodeManger;

    private final InstanceProcessorFactory instanceProcessorFactory;

    public JobExecuteCheckTask(JobInstanceRepository jobInstanceRepository,
                               Broker broker,
                               NodeManger nodeManger,
                               InstanceProcessorFactory instanceProcessorFactory) {
        this.jobInstanceRepository = jobInstanceRepository;
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.instanceProcessorFactory = instanceProcessorFactory;
    }

    public void init() {
        new Timer().schedule(new InnerTask(), 0, Duration.ofSeconds(5).toMillis());
    }

    private class InnerTask extends TimerTask {

        /**
         * 上次检测时间
         */
        private LocalDateTime lastCheckTime = LocalDateTimeUtils.parse("2000-01-01 00:00:00", Formatters.YMD_HMS);

        @Override
        public void run() {
            try {
                // 判断自己是否存在 --- 可能由于心跳异常导致不存活
                if (!nodeManger.alive(broker.getRpcBaseURL().toString())) {
                    return;
                }

                LocalDateTime checkStartTime = lastCheckTime.plusSeconds(-1);
                LocalDateTime checkEndTime = TimeUtils.currentLocalDateTime().plus(-(JobConstant.JOB_REPORT_SECONDS + 5), ChronoUnit.SECONDS);

                Integer limit = 100;
                String startId = "";
                List<JobInstance> jobInstances = jobInstanceRepository.findByExecuteCheck(broker.getRpcBaseURL(), checkStartTime, checkEndTime, startId, limit);
                while (CollectionUtils.isNotEmpty(jobInstances)) {
                    for (JobInstance instance : jobInstances) {
                        CommonThreadPool.IO.submit(() -> {
                            try {
                                JobFeedbackParam param = JobFeedbackParam.builder()
                                        .result(ExecuteResult.FAILED)
                                        .errorMsg(String.format("agent %s is offline", instance.getAgentId()))
                                        .build();
                                InstanceProcessor processor = instanceProcessorFactory.getProcessor(instance.getInstanceType());
                                processor.feedback(instance.getId(), param);
                            } catch (Exception e) {
                                log.error("[JobExecuteCheckTask] handler job fail with error jobInstanceId={}", instance.getId(), e);
                            }
                        });
                    }
                    startId = jobInstances.get(jobInstances.size() - 1).getId();
                    jobInstances = jobInstanceRepository.findByExecuteCheck(broker.getRpcBaseURL(), checkStartTime, checkEndTime, startId, limit);
                }
                lastCheckTime = checkEndTime;
            } catch (Exception e) {
                log.error("[{}] execute fail", this.getClass().getSimpleName(), e);
            }
        }
    }

}
