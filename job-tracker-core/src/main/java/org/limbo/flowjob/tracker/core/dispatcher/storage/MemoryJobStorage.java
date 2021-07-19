package org.limbo.flowjob.tracker.core.dispatcher.storage;

import org.limbo.flowjob.tracker.core.job.context.JobInstance;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 存在内存
 *
 * @author Devil
 * @date 2021/7/17 10:50 下午
 */
public class MemoryJobStorage implements JobStorage {

    private BlockingQueue<JobInstance> queue = new LinkedBlockingQueue<>();

    @Override
    public void store(JobInstance instance) {
        queue.add(instance);
    }

    @Override
    public JobInstance take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            // todo 失败后应该怎么处理
            throw new RuntimeException(e);
        }
    }

    @Override
    public void adjustPriority(String id, int priority) {

    }
}
