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

package org.limbo.flowjob.broker.core.agent;

import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.broker.core.context.job.JobInfo;
import org.limbo.flowjob.broker.core.context.job.JobInstance;

/**
 * @author Devil
 * @since 2022/10/21
 */
public class AgentConverter {

    public static JobSubmitParam toJobDispatchParam(JobInstance instance) {
        JobInfo jobInfo = instance.getJobInfo();

        JobSubmitParam param = new JobSubmitParam();
        param.setJobInstanceId(instance.getId());
        param.setType(jobInfo.getType().type);
        param.setExecutorName(jobInfo.getExecutorName());
        param.setLoadBalanceType(jobInfo.getDispatchOption().getLoadBalanceType().type);
        param.setContext(instance.getContext().toMap());
        param.setAttributes(instance.getAttributes().toMap());
        return param;
    }

}
