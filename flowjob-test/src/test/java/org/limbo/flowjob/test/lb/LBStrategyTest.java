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

package org.limbo.flowjob.test.lb;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.limbo.flowjob.common.rpc.RPCInvocation;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Brozen
 * @since 2022-12-14
 */
class LBStrategyTest {


    private List<IntegerLBServer> servers;


    @BeforeAll
    public void init() {
        servers = IntStream.range(0, 10)
                .mapToObj(IntegerLBServer::new)
                .collect(Collectors.toList());
    }


    @Test
    void testRoundRobin() {
        RoundRobinLBStrategy<IntegerLBServer> strategy = new RoundRobinLBStrategy<>();

        for (int i = 0; i < 200; i++) {
            strategy.select(servers, new RPCInvocation("test", Maps.newHashMap()))
                    .ifPresent(server -> System.out.println("get " + server.getServerId()));
        }
    }


    @Test
    void testWeightedRoundRobin() {
        RoundRobinLBStrategy<IntegerLBServer> strategy = new RoundRobinLBStrategy<>(
                servers -> servers.stream()
                        .collect(Collectors.toMap(
                                IntegerLBServer::getServerId,
                                s -> (s.getValue() + 1) % 7 == 0 ? 9 : 1
                        ))
        );

        for (int i = 0; i < 1000; i++) {
            strategy.select(servers, new RPCInvocation("test", Maps.newHashMap()))
                    .ifPresent(server -> System.out.println("get " + server.getServerId()));
        }
    }



}
