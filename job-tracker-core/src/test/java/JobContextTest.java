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
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.JobReceiveResult;
import org.limbo.flowjob.tracker.commons.exceptions.JobInstanceException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public class JobContextTest {

    private Worker idleWorker;

    private JobTracker jobTracker;

    private HashedWheelTimerScheduler scheduler;

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
            public Mono<JobReceiveResult> sendJobContext(JobInstance context) throws JobWorkerException {
                return null;
            }

            @Override
            public void unregister() {

            }

        };
    }

    @Test
    public void testSimpleJobContext() throws JobInstanceException {
        JobInstance context = new JobInstance(new JobInstanceRepository() {
            @Override
            public void updateInstance(JobInstance context) {
                System.out.println("updateContext");
            }
        });
        context.setJobInstanceId("ctx1");
        context.setJobId("job1");
        context.setState(JobScheduleStatus.Scheduling);
        context.setJobAttributes(null);

        context.onContextAccepted().subscribe(c -> System.out.println(c.getWorkerId() + " accepted"));
        context.onContextRefused().subscribe(c -> System.out.println(c.getWorkerId() + " refused"));
        context.onContextClosed().subscribe(c -> System.out.println(c.getJobInstanceId() + " closed"));

        context.startupContext(idleWorker);
        context.acceptContext(idleWorker);
        context.closeContext();
    }


}
