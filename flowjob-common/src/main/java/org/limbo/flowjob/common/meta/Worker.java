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

package org.limbo.flowjob.common.meta;

import lombok.Getter;
import org.limbo.flowjob.common.lb.LBServer;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Getter
public class Worker implements LBServer {

    private String id;

    private URL url;

    private List<WorkerExecutor> executors;

    /**
     * Worker 标签
     */
    private Map<String, List<String>> tags;

    /**
     * Worker 状态指标
     */
    private WorkerMetric metric;

    @Override
    public String getServerId() {
        return id;
    }

    @Override
    public boolean isAlive() {
        return true;
    }
}
