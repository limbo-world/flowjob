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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.limbo.flowjob.broker.application.plan.service.TaskService;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimerTask;

/**
 * 处理 调度中状态的job 修改为执行中
 * 看 TaskService.taskFail TaskService.taskSuccess 的处理
 * 如果事务成功 但是job调度执行完成前失败了 会导致task和job状态变更没有持久化
 */
@Component
public class JobStatusCheckTask extends TimerTask {

    @Setter(onMethod_ = @Inject)
    private JobScheduler scheduler;
    @Setter(onMethod_ = @Inject)
    private TaskService taskService;

    @Override
    public void run() {
        List<JobInstance> jobInstances = unSchedules(TimeUtil.currentLocalDateTime().plusSeconds(10));
        for (JobInstance jobInstance : jobInstances) {
            scheduler.schedule(jobInstance);
        }
    }

    /**
     * 查询超时未下发的job todo 根据自己plan去查找
     */
    public List<JobInstance> unSchedules(LocalDateTime nextTriggerAt) {
        return null;
    }
}
