import org.junit.Before;
import org.junit.Test;
import org.limbo.flowjob.tracker.commons.beans.job.valueobject.JobAttributes;
import org.limbo.flowjob.tracker.commons.beans.job.domain.JobContext;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.utils.JacksonUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Brozen
 * @since 2021-05-31
 */
public class TestValueObjectJackson {

    private Map<String, List<String>> attr;

    @Before
    public void initAttr() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("name", Arrays.asList("brozen", "lau"));
        attributes.put("age", Collections.singletonList("18"));
        this.attr = attributes;
    }

    @Test
    public void testJobAttribute() {

        JobAttributes attributes = new JobAttributes(attr);

        String json = JacksonUtils.toJSONString(attributes);
        System.out.println("Serialized ==>" + json);

        JobAttributes attributes1 = JacksonUtils.parseObject(json, JobAttributes.class);
        System.out.println("Deserialized ==> " + attributes1);

    }

    @Test
    public void testJobContext() {
        JobContext jobContext = new JobContext();
        jobContext.setContextId("ctx1");
        jobContext.setJobId("job1");
        jobContext.setStatus(JobContextStatus.INIT);
        jobContext.setWorkerId("");
        jobContext.setCreatedAt(LocalDateTime.now());
        jobContext.setJobAttributes(new JobAttributes(attr));

        String json = JacksonUtils.toJSONString(jobContext);
        System.out.println("Serialized ==>" + json);

        JobContext jobContext1 = JacksonUtils.parseObject(json, JobContext.class);
        System.out.println("Deserialized ==> " + jobContext1);

    }

}
