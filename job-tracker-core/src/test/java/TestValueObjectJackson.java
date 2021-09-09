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
import org.limbo.flowjob.tracker.commons.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.tracker.core.job.context.Attributes;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.utils.JacksonUtils;

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

        Attributes attributes = new Attributes(attr);

        String json = JacksonUtils.toJSONString(attributes);
        System.out.println("Serialized ==>" + json);

        Attributes attributes1 = JacksonUtils.parseObject(json, Attributes.class);
        System.out.println("Deserialized ==> " + attributes1);

    }

    @Test
    public void testJobContext() {
        Task task = new Task();
        task.setJobId("job1");
        task.setState(TaskScheduleStatus.SCHEDULING);
        task.setWorkerId("");
        task.setAttributes(new Attributes(attr));

        String json = JacksonUtils.toJSONString(task);
        System.out.println("Serialized ==>" + json);

        Task jobContext1 = JacksonUtils.parseObject(json, Task.class);
        System.out.println("Deserialized ==> " + jobContext1);

    }

}
