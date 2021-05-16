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

package org.limbo.flowjob.tracker.core;

import java.util.List;

/**
 * 作业分发节点抽象。
 *
 * @author Brozen
 * @since 2021-05-16
 */
public interface JobTracker {


    /**
     * 获取所有可用的worker。可用指在调用此方法之时，心跳检测没有失败的worker。
     * @return 可用的worker。
     */
    List<Worker> availableWorkers();


}
