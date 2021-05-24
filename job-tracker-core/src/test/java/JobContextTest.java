import org.junit.Before;
import org.junit.Test;
import org.limbo.flowjob.tracker.core.exceptions.JobContextException;
import org.limbo.flowjob.tracker.core.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.JobContext;
import org.limbo.flowjob.tracker.core.job.JobContextRepository;
import org.limbo.flowjob.tracker.core.job.JobContextStatus;
import org.limbo.flowjob.tracker.core.job.SimpleJobContext;
import org.limbo.flowjob.tracker.core.messaging.SendJobResult;
import org.limbo.flowjob.tracker.core.scheduler.HashedWheelTimerJobScheduler;
import org.limbo.flowjob.tracker.core.tracker.DisposableJobTracker;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.JobTrackerLifecycle;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerMetric;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
        this.idleWorker = new Worker() {
            @Override
            public String getId() {
                return "w1";
            }

            @Override
            public WorkerMetric metric() {
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
            public Mono<Void> onUnregistered() {
                return null;
            }
        };
    }

    @Test
    public void testSimpleJobContext() throws JobContextException {
        SimpleJobContext context = new SimpleJobContext("job1", "ctx1", JobContextStatus.INIT, null, new JobContextRepository() {
            @Override
            public void updateContext(JobContext context) {
                System.out.println("updateContext");
            }
        });

        context.onContextAccepted().subscribe(c -> System.out.println(c.getWorkerId() + " accepted"));
        context.onContextRefused().subscribe(c -> System.out.println(c.getWorkerId() + " refused"));
        context.onContextClosed().subscribe(c -> System.out.println(c.getContextId() + " closed"));

        context.startupContext(idleWorker);
        context.acceptContext(idleWorker);
        context.closeContext();
    }


}
