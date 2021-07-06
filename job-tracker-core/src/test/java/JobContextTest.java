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

import org.junit.Before;
import org.junit.Test;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.SendJobResult;
import org.limbo.flowjob.tracker.commons.exceptions.JobContextException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.flowjob.tracker.core.scheduler.HashedWheelTimerJobScheduler;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public class JobContextTest {

    private Worker idleWorker;

    private JobTracker jobTracker;

    private HashedWheelTimerJobScheduler scheduler;

    @Before
    public void init() {
        this.idleWorker = new Worker(null, null, null) {
            @Override
            public String getWorkerId() {
                return "";
            }

            @Override
            public WorkerMetric getMetric() {
                return null;
            }

            @Override
            public WorkerStatus getStatus() {
                return null;
            }

            @Override
            public Mono<WorkerMetric> ping() {
                return null;
            }

            @Override
            public Mono<SendJobResult> sendJobContext(JobContext context) throws JobWorkerException {
                return null;
            }

            @Override
            public void unregister() {

            }

        };
    }

    @Test
    public void testSimpleJobContext() throws JobContextException {
        JobContext context = new JobContext(new JobContextRepository() {
            @Override
            public void updateContext(JobContext context) {
                System.out.println("updateContext");
            }
        });
        context.setJobId("job1");
        context.setContextId("ctx1");
        context.setStatus(JobContextStatus.INIT);
        context.setJobAttributes(null);

        context.onContextAccepted().subscribe(c -> System.out.println(c.getWorkerId() + " accepted"));
        context.onContextRefused().subscribe(c -> System.out.println(c.getWorkerId() + " refused"));
        context.onContextClosed().subscribe(c -> System.out.println(c.getContextId() + " closed"));

        context.startupContext(idleWorker);
        context.acceptContext(idleWorker);
        context.closeContext();
    }


}
