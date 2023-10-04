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

package org.limbo.flowjob.broker.application.support;

import org.limbo.flowjob.api.param.PageParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author KaiFengCai
 * @since 2023/1/30
 */
public class JpaHelper {

    /**
     * PageParam 转换为 Pageable
     */
    public static Pageable pageable(PageParam param) {
        // PageParam 从 1 开始 Pageable 从 0 开始
        return PageRequest.of(param.getCurrent() - 1, param.getSize());
    }

}
