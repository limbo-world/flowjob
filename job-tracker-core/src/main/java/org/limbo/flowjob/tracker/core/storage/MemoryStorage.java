package org.limbo.flowjob.tracker.core.storage;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 存在内存
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class MemoryStorage implements Storage {

    private final BlockingQueue<Storable> queue = new LinkedBlockingQueue<>();

    @Override
    public void store(Storable storable) {
        queue.add(storable);
    }

    @Override
    public Storable take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            // todo 失败后应该怎么处理
            throw new RuntimeException(e);
        }
    }

}
