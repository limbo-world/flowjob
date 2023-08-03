/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.worker.demo.executors;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.executor.MapReduceTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/2/7
 */
@Slf4j
@Component
public class MapReduceExecutorDemo extends MapReduceTaskExecutor {

    private static final String KEY = "k";

    private static final String COUNT_KEY = "count";

    private static final String NUM_KEY = "num";

    @Override
    public List<Map<String, Object>> sharding(Task task) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> taskParam = new HashMap<>();
            taskParam.put(KEY, i);
            // add context
            task.setContextValue("t" + KEY, i);
            // add task
            result.add(taskParam);
        }
        // job
        testPutJobAttr(task);
        return result;
    }

    @Override
    public Map<String, Object> map(Task task) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> mapAttributes = task.getMapAttributes();
        int v = (int) mapAttributes.get(KEY) + 100;
        result.put(KEY, v);
        // add context
        String ck = "m" + KEY;
        if (task.getContextValue(ck) != null) {
            v += (int) task.getContextValue(ck);
        }
        task.setContextValue("m" + KEY, v);
        // job
        testPutJobAttr(task);
        return result;
    }

    @Override
    public void reduce(Task task) {
        List<Map<String, Object>> reduceAttributes = task.getReduceAttributes();
        int sum = 0;
        for (Map<String, Object> reduceAttribute : reduceAttributes) {
            int v = (int) reduceAttribute.get(KEY);
            sum += v;
        }
        task.setContextValue("sum", sum);
        // job
        testPutJobAttr(task);
        log.info("sum = {}", sum);
    }

    private void testPutJobAttr(Task task) {
        if (task.getJobAttribute(COUNT_KEY) == null) {
            task.setJobAttribute(COUNT_KEY, 1);
        } else {
            task.setJobAttribute(COUNT_KEY, 1 + (int) task.getJobAttribute(COUNT_KEY));
        }

        if (task.getJobAttribute(NUM_KEY) == null) {
            task.setJobAttribute(NUM_KEY, 10);
        } else {
            task.setJobAttribute(NUM_KEY, 10 + (int) task.getJobAttribute(NUM_KEY));
        }
    }

}
