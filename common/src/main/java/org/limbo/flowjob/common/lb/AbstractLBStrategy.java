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

package org.limbo.flowjob.common.lb;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2022-09-02
 */
@Slf4j
public abstract class AbstractLBStrategy<S extends LBServer> implements LBStrategy<S> {

    @Override
    public Optional<S> select(List<S> servers) {
        // 有服务存在，但是如果所有服务都挂了的话，也返回空
        if (CollectionUtils.isEmpty(servers)) {
            log.warn("No alive server for load strategy [{}]", getClass().getName());
            return Optional.empty();
        }
        return selectNonEmpty(servers);
    }

    /**
     * 从非空列表选取对象
     *
     * @param servers 非空
     * @return S
     */
    protected abstract Optional<S> selectNonEmpty(List<S> servers);
}
