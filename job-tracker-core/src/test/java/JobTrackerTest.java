import org.junit.Test;
import org.limbo.flowjob.tracker.core.tracker.DisposableJobTracker;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.ReactorJobTrackerLifecycle;

/**
 * @author Brozen
 * @since 2021-06-01
 */
public class JobTrackerTest {

    /**
     * 测试下，Sinks.many().multicast().directAllOrNothing(); 只有在Subscriber订阅之后发射的信号才会有效，因为没有缓存。
     */
    @Test
    public void testJobTrackerLifecycle() {
        ReactorJobTrackerLifecycle lifecycle = new ReactorJobTrackerLifecycle();
        lifecycle.beforeStart().subscribe(t -> System.out.println("1" + t), System.err::println, () -> System.out.println("OK"));

//        lifecycle.triggerBeforeStart(new DisposableJobTracker() {
//            @Override
//            public JobTracker jobTracker() {
//                return null;
//            }
//
//            @Override
//            public void dispose() {
//
//            }
//        });
        lifecycle.beforeStart().subscribe(t -> System.out.println("2" + t));

    }

}
