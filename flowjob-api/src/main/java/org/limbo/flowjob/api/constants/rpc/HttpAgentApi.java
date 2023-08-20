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

package org.limbo.flowjob.api.constants.rpc;

/**
 * @author Devil
 * @since 2022/11/7
 */
public interface HttpAgentApi {

    String API_JOB_RECEIVE = "/api/v1/rpc/job";

    String API_TASK_RECEIVE = "/api/v1/rpc/task";

    String API_TASK_REPORT = "/api/v1/rpc/task/report";

    String API_TASK_FEEDBACK = "/api/v1/rpc/task/feedback";

    String API_TASK_PAGE = "/api/v1/rpc/task/page";

}
