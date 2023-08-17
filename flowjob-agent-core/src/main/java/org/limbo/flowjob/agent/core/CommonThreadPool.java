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

package org.limbo.flowjob.agent.core;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Devil
 * @since 2023/5/12
 */
public class CommonThreadPool {

    public static final ThreadPoolExecutor CPU = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(10240),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static final ThreadPoolExecutor IO = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 4,
            Runtime.getRuntime().availableProcessors() * 4,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(10240),
            new ThreadPoolExecutor.CallerRunsPolicy());

}
