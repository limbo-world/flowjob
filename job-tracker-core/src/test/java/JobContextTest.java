import org.junit.Before;
import org.junit.Test;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.exceptions.JobContextException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.flowjob.tracker.commons.beans.dto.SendJobResult;
import org.limbo.flowjob.tracker.core.scheduler.HashedWheelTimerJobScheduler;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.commons.beans.domain.worker.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerDO;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public class JobContextTest {

    private WorkerDO idleWorker;

    private JobTracker jobTracker;

    private HashedWheelTimerJobScheduler scheduler;

    @Before
    public void init() {
        this.idleWorker = new WorkerDO() {
            @Override
            public String getId() {
                return "w1";
            }

            @Override
            public WorkerProtocol getProtocol() {
                return null;
            }

            @Override
            public String getIp() {
                return null;
            }

            @Override
            public Integer getPort() {
                return 0;
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
            public Mono<SendJobResult> sendJobContext(JobContextDO context) throws JobWorkerException {
                return null;
            }

            @Override
            public void unregister() {

            }

        };
    }

    @Test
    public void testSimpleJobContext() throws JobContextException {
        JobContextDO context = new JobContextDO(new JobContextRepository() {
            @Override
            public void updateContext(JobContextDO context) {
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
