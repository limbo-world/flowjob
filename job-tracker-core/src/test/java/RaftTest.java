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

import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import org.junit.Test;
import org.limbo.flowjob.tracker.core.raft.ElectionNode;
import org.limbo.flowjob.tracker.core.raft.ElectionNodeOptions;
import org.limbo.flowjob.tracker.core.raft.LeaderStateListener;

import java.util.Collections;
import java.util.Scanner;

/**
 * @author Devil
 * @date 2021/6/4 4:59 下午
 */
public class RaftTest {

    // 先启动 1 2 3 4 5    123在启动后就选举成功了，然后逐个关闭主节点
    @Test
    public void testElectionNormal1() {
        bootstrapNormal("/data/server1", "election_test", "127.0.0.1:8081", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
        // wait
        new Scanner(System.in).next();
    }

    @Test
    public void testElectionNormal2() {
        bootstrapNormal("/data/server2", "election_test", "127.0.0.1:8082", "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
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

    public void bootstrapNormal(String dataPath, String groupId, String serverIdStr, String initialConfStr) {
        final ElectionNodeOptions electionOpts = new ElectionNodeOptions();
        electionOpts.setDataPath(dataPath);
        electionOpts.setGroupId(groupId);
        electionOpts.setServerAddress(serverIdStr);
        electionOpts.setInitialServerAddressList(initialConfStr);

        final ElectionNode node = new ElectionNode();
        node.addLeaderStateListener(new LeaderStateListener() {

            @Override
            public void onLeaderStart(long leaderTerm) {
                PeerId serverId = node.getNode().getLeaderId();
                String ip = serverId.getIp();
                int port = serverId.getPort();
                System.out.println("[ElectionBootstrap] Leader's ip is: " + ip + ", port: " + port);
                System.out.println("[ElectionBootstrap] Leader start on term: " + leaderTerm);
            }

            @Override
            public void onLeaderStop(long leaderTerm) {
                System.out.println("[ElectionBootstrap] Leader stop on term: " + leaderTerm);
            }
        });
        node.init(electionOpts);
    }


}
