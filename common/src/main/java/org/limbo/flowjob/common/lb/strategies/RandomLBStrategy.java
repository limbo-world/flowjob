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

package org.limbo.flowjob.common.lb.strategies;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.lb.AbstractLBStrategy;
import org.limbo.flowjob.common.lb.Invocation;
import org.limbo.flowjob.common.lb.LBServer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Brozen
 * @since 2022-09-02
 */
// todo 考虑权重/优先级？
@Slf4j
public class RandomLBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    @Override
    protected Optional<S> doSelect(List<S> servers, Invocation invocation) {
        return Optional.of(servers.get(ThreadLocalRandom.current().nextInt(servers.size())));
    }
}
