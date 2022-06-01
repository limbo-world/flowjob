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

package org.limbo.flowjob.broker.core.exceptions;

import lombok.Getter;

/**
 * 作业下发给worker时，发生的异常
 *
 * @author Brozen
 * @since 2021-05-14
 */
public class TaskReceiveException extends JobExecuteException {

    /**
     * 不可用的worker
     */
    @Getter
    private String workerId;

    public TaskReceiveException(String jobId, String workerId, String message) {
        super(jobId, message);
        this.workerId = workerId;
    }

    public TaskReceiveException(String jobId, String workerId, String message, Throwable cause) {
        super(jobId, message, cause);
        this.workerId = workerId;
    }

}
