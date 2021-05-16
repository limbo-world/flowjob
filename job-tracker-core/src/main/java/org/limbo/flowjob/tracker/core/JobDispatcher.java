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

/**
 * 作业分发器。封装了作业分发时，worker的选择规则：
 * 1. round robin
 * 2. random
 * 3. N-th
 * 4. consistent hash
 * 5. fail over
 * 6. busy over
 * 7. shard broadcast
 * 8. map reduce
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface JobDispatcher {

    /**
     * 分发一个作业给worker执行，并返回此作业执行的上下文。
     * @param job 待分发的作业
     * @return 作业执行上下文
     */
    JobContext dispatch(Job job);

}
