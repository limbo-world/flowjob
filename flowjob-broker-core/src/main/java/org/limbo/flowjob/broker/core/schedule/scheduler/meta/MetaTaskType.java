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

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Devil
 * @since 2022/12/18
 */
public enum MetaTaskType {

    PLAN,

    PLAN_INSTANCE,

    PLAN_LOAD,

    UPDATED_PLAN_LOAD,

    PLAN_EXECUTE_CHECK,

    JOB_EXECUTE_CHECK,

    JOB,

    WORKER_OFFLINE,

    ;

    public static MetaTaskType parse(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        for (MetaTaskType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

}
