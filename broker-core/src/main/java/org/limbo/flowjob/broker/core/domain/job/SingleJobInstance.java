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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Devil
 * @since 2023/1/29
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SingleJobInstance extends JobInstance {

    private static final long serialVersionUID = 5069250526499563501L;

    /**
     * 任务信息
     */
    private JobInfo jobInfo;

    @Override
    public JobInfo getJobInfo() {
        return jobInfo;
    }
}
