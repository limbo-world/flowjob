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

package org.limbo.flowjob.tracker.core.exceptions;

import lombok.Getter;
import org.limbo.flowjob.tracker.core.Job;
import org.limbo.flowjob.tracker.core.Worker;

/**
 * @author Brozen
 * @since 2021-05-14
 */
public class WorkerUnavailableException extends JobExecuteException {

    /**
     * 不可用的worker
     */
    @Getter
    private Worker worker;

    public WorkerUnavailableException(Job job, Worker worker, String message) {
        super(job, message);
        this.worker = worker;
    }

    public WorkerUnavailableException(Job job, Worker worker, String message, Throwable cause) {
        super(job, message, cause);
        this.worker = worker;
    }

}
