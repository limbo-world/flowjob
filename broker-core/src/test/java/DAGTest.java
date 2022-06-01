import org.junit.Test;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Devil
 * @since 2021/8/30
 */
public class DAGTest {

    @Test
    public void testNoRoot() {
        Job job = job("1", Collections.singleton("2"));
        Job job2 = job("2", Collections.singleton("3"));
        Job job3 = job("3", Collections.singleton("1"));

        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        jobs.add(job2);
        jobs.add(job3);

        try {
            JobDAG dag = new JobDAG(jobs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testCyclic() {
        Job job = job("1", Collections.singleton("3"));
        Job job2 = job("2", Collections.singleton("3"));
        Job job3 = job("3", Collections.singleton("4"));
        Job job4 = job("4", Collections.singleton("3"));

        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        jobs.add(job2);
        jobs.add(job3);
        jobs.add(job4);

        try {
            JobDAG dag = new JobDAG(jobs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Job job(String id, Set<String> childrenIds) {
        Job job = new Job();
        job.setJobId(id);
        job.setChildrenIds(childrenIds);
        return job;
    }
}
