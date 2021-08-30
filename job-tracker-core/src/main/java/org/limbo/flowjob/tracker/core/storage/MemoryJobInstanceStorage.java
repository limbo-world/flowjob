package org.limbo.flowjob.tracker.core.storage;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 存在内存
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class MemoryJobInstanceStorage implements JobInstanceStorage {

    private final BlockingQueue<JobInstance> queue = new LinkedBlockingQueue<>();

    @Override
    public void store(JobInstance instance) {
        queue.add(instance);
        if (log.isDebugEnabled()) {
            log.debug("Store JobInstance " + instance.getId());
        }
    }

    @Override
    public JobInstance take() {
        try {
            JobInstance jobInstance = queue.take();
            if (log.isDebugEnabled()) {
                log.debug("Take JobInstance " + jobInstance.getId());
            }
            return jobInstance;
        } catch (InterruptedException e) {
            // todo 失败后应该怎么处理
            throw new RuntimeException(e);
        }
    }

}
