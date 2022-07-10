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
package org.limbo.flowjob.broker.cluster.election;

import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.entity.LeaderChangeContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Devil
 * @since 2021/8/9
 */
@Slf4j
public class ElectionStateMachine extends StateMachineAdapter {

    private final AtomicLong leaderTerm = new AtomicLong(-1L);
    private final List<StateListener> listeners;

    public ElectionStateMachine(List<StateListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onApply(final Iterator it) {
        // election only, do nothing
        while (it.hasNext()) {
            log.info("On apply with term: {} and index: {}. ", it.getTerm(), it.getIndex());
            it.next();
        }
    }

    @Override
    public void onLeaderStart(final long term) {
        super.onLeaderStart(term);
        this.leaderTerm.set(term);
        for (final StateListener listener : this.listeners) { // iterator the snapshot
            listener.onLeaderStart(term);
        }
    }

    @Override
    public void onLeaderStop(final Status status) {
        super.onLeaderStop(status);
        final long oldTerm = leaderTerm.get();
        this.leaderTerm.set(-1L);
        for (final StateListener listener : this.listeners) { // iterator the snapshot
            listener.onLeaderStop(oldTerm);
        }
    }

    @Override
    public void onStopFollowing(final LeaderChangeContext ctx) {
        super.onStartFollowing(ctx);

        for (final StateListener listener : listeners) { // iterator the snapshot
            listener.onStopFollowing(ctx.getLeaderId(), ctx.getTerm());
        }
    }

    @Override
    public void onStartFollowing(final LeaderChangeContext ctx) {
        super.onStopFollowing(ctx);

        for (final StateListener listener : listeners) { // iterator the snapshot
            listener.onStartFollowing(ctx.getLeaderId(), ctx.getTerm());
        }
    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    public void addStateListener(final StateListener listener) {
        this.listeners.add(listener);
    }
}
