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

package org.limbo.flowjob.broker.core.meta.job;

import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.net.URL;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/12/30
 */
public class JobInstanceFactory {

    public static JobInstance create(String id, String instanceId, InstanceType instanceType, URL brokerUrl, Attributes planAttributes, Attributes context, WorkflowJobInfo jobInfo, LocalDateTime triggerAt) {
        Attributes attributes = new Attributes();
        attributes.put(planAttributes);
        attributes.put(jobInfo.getAttributes());
        return JobInstance.builder()
                .id(id)
                .agentId("")
                .jobInfo(jobInfo)
                .brokerUrl(brokerUrl)
                .instanceId(instanceId)
                .instanceType(instanceType)
                .status(JobStatus.SCHEDULING)
                .triggerAt(triggerAt)
                .context(context == null ? new Attributes() : context)
                .attributes(attributes)
                .build();
    }
}
