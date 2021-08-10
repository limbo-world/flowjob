/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.alipay.sofa.jraft.*;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import org.junit.Test;
import org.limbo.flowjob.tracker.core.raft.ElectionNode;
import org.limbo.flowjob.tracker.core.raft.ElectionNodeOptions;
import org.limbo.flowjob.tracker.core.raft.StateListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * @author Devil
 * @since 2021/7/24
 */
public class RaftTest {

    String groupId = "election_test";

    @Test
    public void testSingle() {
        bootstrapNormal("/data/server1", groupId, "127.0.0.1:8081", "127.0.0.1:8081");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testRouteTable() throws TimeoutException, InterruptedException {
        String configString = "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084,127.0.0.1:8085";
        List<ElectionNode> nodes = new ArrayList<>();
        nodes.add(bootstrapNormal("/data/server1", groupId, "127.0.0.1:8081", configString));
        nodes.add(bootstrapNormal("/data/server2", groupId, "127.0.0.1:8082", configString));
        nodes.add(bootstrapNormal("/data/server3", groupId, "127.0.0.1:8083", configString));

        Thread.sleep(2000);

        for (ElectionNode node : nodes) {
            if (node.isLeader()) {
                System.out.println(node.getNode().getLeaderId());
            }
        }

        Configuration conf = new Configuration();
        if (!conf.parse(configString)) {
            throw new IllegalArgumentException("Fail to parse conf:" + configString);
        }
        RouteTable.getInstance().updateConfiguration(groupId, conf);

        final CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());

        Status status = RouteTable.getInstance().refreshLeader(cliClientService, groupId, 1000);
        if (!status.isOk()) {
            System.out.println(status);
        }

        final PeerId leader = RouteTable.getInstance().selectLeader(groupId);
        System.out.println(leader);

        // wait
        new Scanner(System.in).next();
    }

    /**
     * 重新选举
     * @throws InterruptedException
     */
    @Test
    public void testReElection() throws InterruptedException {
        String configString = "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084,127.0.0.1:8085";
        List<ElectionNode> nodes = new ArrayList<>();
        nodes.add(bootstrapNormal("/data/server1", groupId, "127.0.0.1:8081", configString));
        nodes.add(bootstrapNormal("/data/server2", groupId, "127.0.0.1:8082", configString));
        nodes.add(bootstrapNormal("/data/server3", groupId, "127.0.0.1:8083", configString));
        nodes.add(bootstrapNormal("/data/server4", groupId, "127.0.0.1:8084", configString));
        nodes.add(bootstrapNormal("/data/server5", groupId, "127.0.0.1:8085", configString));

        Thread.sleep(5000);

        System.out.println("==========");

        for (ElectionNode node : nodes) {
            if (node.isLeader()) {
                node.shutdown();
            }
        }

        Thread.sleep(3000);

        System.out.println("=========");

        for (ElectionNode node : nodes) {
            System.out.println(node.getNode().getNodeId() + " " + node.getNode().isLeader());
        }

        // wait
        new Scanner(System.in).next();
    }


    // 先启动 1 2 3 4 5    123在启动后就选举成功了，然后逐个关闭主节点
    @Test
    public void testElectionNormal1() {
        bootstrapNormal("/data/server1", groupId, "127.0.0.1:8081", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testElectionNormal2() {
        bootstrapNormal("/data/server2", groupId, "127.0.0.1:8082", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testElectionNormal3() {
        bootstrapNormal("/data/server3", "election_test", "127.0.0.1:8083", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testElectionNormal4() {
        bootstrapNormal("/data/server4", "election_test", "127.0.0.1:8084", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testElectionNormal5() {
        bootstrapNormal("/data/server5", "election_test", "127.0.0.1:8085", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084,127.0.0.1:8085");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testAddPeer() {
        CliService andInitCliService = RaftServiceFactory.createAndInitCliService(new CliOptions());
        Configuration conf = JRaftUtils.getConfiguration("127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084,127.0.0.1:8085");
        andInitCliService.addPeer("election_test", conf, new PeerId("127.0.0.1", 8081));
        andInitCliService.shutdown();
    }

    @Test
    public void testRemoveLeader() {
        CliService andInitCliService = RaftServiceFactory.createAndInitCliService(new CliOptions());
        Configuration conf = JRaftUtils.getConfiguration("127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084,127.0.0.1:8085");
        andInitCliService.removeLearners("election_test", conf, Collections.singletonList(new PeerId("127.0.0.1", 8081)));
        andInitCliService.shutdown();
    }

    @Test
    public void testRemovePeer() {
        CliService andInitCliService = RaftServiceFactory.createAndInitCliService(new CliOptions());
        Configuration conf = JRaftUtils.getConfiguration("127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083,127.0.0.1:8084,127.0.0.1:8085");
        andInitCliService.removePeer("election_test", conf, new PeerId("127.0.0.1", 8081));
        andInitCliService.shutdown();
    }

    public ElectionNode bootstrapNormal(String dataPath, String groupId, String serverIdStr, String initialConfStr) {
        final ElectionNodeOptions electionOpts = new ElectionNodeOptions();
        electionOpts.setDataPath(dataPath);
        electionOpts.setGroupId(groupId);
        electionOpts.setServerAddress(serverIdStr);
        electionOpts.setServerAddressList(initialConfStr);

        final ElectionNode node = new ElectionNode();
        node.addStateListener(new StateListener() {

            @Override
            public void onLeaderStart(long leaderTerm) {
                PeerId serverId = node.getNode().getLeaderId();
                String ip = serverId.getIp();
                int port = serverId.getPort();
                System.out.println(node.getNode().getNodeId() + ": Leader's ip is: " + ip + ", port: " + port);
            }

            @Override
            public void onLeaderStop(long leaderTerm) {
                PeerId serverId = node.getNode().getLeaderId();
                String ip = serverId.getIp();
                int port = serverId.getPort();
                System.out.println(node.getNode().getNodeId() + ": Leader's stop on term: " + ip + ", port: " + port);
            }

            @Override
            public void onStartFollowing(PeerId newLeaderId, long newTerm) {
                System.out.println(node.getNode().getNodeId() + ": Follow Leader on term: " + newLeaderId);
            }

            @Override
            public void onStopFollowing(PeerId oldLeaderId, long oldTerm) {
                System.out.println(node.getNode().getNodeId() + ": Change Leader to term: " + oldLeaderId);
            }
        });
        node.init(electionOpts);
        return node;
    }


}
