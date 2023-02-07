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

import com.google.common.collect.Maps;
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
public class MapReduceExecutorDemo implements MapReduceTaskExecutor {

    @Override
    public List<Map<String, Object>> split(Task task) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> taskParam = new HashMap<>();
            taskParam.put("t" + i, i);
            result.add(taskParam);
        }
        return result;
    }

    @Override
    public Map<String, Object> map(Task task) {
        return null;
    }

    @Override
    public void reduce(Task task) {

    }

}
