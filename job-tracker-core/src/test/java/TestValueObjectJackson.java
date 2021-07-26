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
import org.limbo.flowjob.tracker.core.job.context.JobAttributes;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
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
        JobInstance JobContext = new JobInstance(null);
        JobContext.setJobId("job1");
        JobContext.setState(JobScheduleStatus.Scheduling);
        JobContext.setWorkerId("");
        JobContext.setJobAttributes(new JobAttributes(attr));

        String json = JacksonUtils.toJSONString(JobContext);
        System.out.println("Serialized ==>" + json);

        JobInstance jobContext1 = JacksonUtils.parseObject(json, JobInstance.class);
        System.out.println("Deserialized ==> " + jobContext1);

    }

}
