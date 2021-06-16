package org.limbo.flowjob.tracker.core.tracker;

import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 基于RSocket协议通信的远程JobTracker
 * TODO
 *
 * @author Brozen
 * @since 2021-06-16
 */
public class RSocketRemoteJobTracker extends RemoteJobTracker {
    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public Mono<Worker> registerWorker(Worker worker) {
        return null;
    }

    @Override
    public List<Worker> availableWorkers() {
        return null;
    }

    @Override
    public Mono<Worker> unregisterWorker(String workerId) {
        return null;
    }
}
