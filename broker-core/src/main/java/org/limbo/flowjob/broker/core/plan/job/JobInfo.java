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

package org.limbo.flowjob.broker.core.plan.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.plan.job.dag.DAGNode;

import java.util.Set;

/**
 * 作业的抽象。主要定义了作业领域的的行为方法，属性的访问操作在{@link JobInfo}轻量级领域对象中。
 *
 * todo
 * 允许失败的任务，失败后继续执行下面节点
 * 任务失败后处理情况，1. 整个plan失败 2. 执行另一个A分支（成功的话执行B分支）
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class JobInfo extends DAGNode {

    private static final long serialVersionUID = 5340755318570959476L;

    /**
     * 作业ID
     */
    private String jobId;

    private String name;

    /**
     * 作业描述
     */
    private String description;

    /**
     * 触发类型
     */
    private TriggerType triggerType;

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private ExecutorOption executorOption;

    public JobInfo(String id, Set<String> childrenIds) {
        super(id, childrenIds);
    }


    /**
     * 生成作业实例，新生成的作业状态是 {@link JobStatus#DISPATCHING}
     */
    public JobInstance newInstance(String planId, String planInstanceId) {
        JobInstance jobInstance = new JobInstance();
        jobInstance.setPlanId(planId);
        jobInstance.setPlanInstanceId(planInstanceId);
        jobInstance.setJobId(jobId);
        jobInstance.setStatus(JobStatus.DISPATCHING);
        jobInstance.setAttributes(null); // todo
        jobInstance.setTaskCreatorFactory(null); // todo
        return jobInstance;
    }

}
