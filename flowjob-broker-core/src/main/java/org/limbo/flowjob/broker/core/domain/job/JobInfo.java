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

package org.limbo.flowjob.broker.core.domain.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.broker.core.worker.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.worker.dispatch.RetryOption;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.io.Serializable;

/**
 * 作业的抽象。主要定义了作业领域的的行为方法，属性的访问操作在{@link JobInfo}轻量级领域对象中。
 * 属性修改会对后续调度产生影响
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo implements Serializable {

    private static final long serialVersionUID = 5053574296501459651L;

    private String id;

    /**
     * 类型
     */
    private JobType type;

    /**
     * 作业执行器配置参数
     */
    private String executorName;

    /**
     * 重试参数
     */
    private RetryOption retryOption;

    /**
     * 属性参数
     */
    protected Attributes attributes;

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

}
