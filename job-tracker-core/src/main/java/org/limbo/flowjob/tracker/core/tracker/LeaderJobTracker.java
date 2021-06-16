package org.limbo.flowjob.tracker.core.tracker;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;

import java.util.LinkedList;
import java.util.List;

/**
 * 主从模式下，主节点JobTracker
 * TODO
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Slf4j
public class LeaderJobTracker extends LocalJobTracker {

    /**
     * 当前节点是主节点时，此属性代表从节点tracker列表
     */
    protected List<RemoteJobTracker> followers;

    public LeaderJobTracker(WorkerRepository workerRepository) {
        super(workerRepository);
        this.followers = new LinkedList<>();
    }

}
