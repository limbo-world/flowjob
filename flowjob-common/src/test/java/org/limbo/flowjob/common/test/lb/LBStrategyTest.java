package org.limbo.flowjob.common.test.lb;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.limbo.flowjob.common.lb.RPCInvocation;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Brozen
 * @since 2022-12-14
 */
public class LBStrategyTest {


    private List<IntegerLBServer> servers;


    @Before
    public void init() {
        servers = IntStream.range(0, 10)
                .mapToObj(IntegerLBServer::new)
                .collect(Collectors.toList());
    }


    @Test
    public void testRoundRobin() {
        RoundRobinLBStrategy<IntegerLBServer> strategy = new RoundRobinLBStrategy<>();

        for (int i = 0; i < 200; i++) {
            strategy.select(servers, new RPCInvocation("test", Maps.newHashMap()))
                    .ifPresent(server -> System.out.println("get " + server.getServerId()));
        }
    }


    @Test
    public void testWeightedRoundRobin() {
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
