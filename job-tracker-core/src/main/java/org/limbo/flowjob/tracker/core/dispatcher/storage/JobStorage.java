package org.limbo.flowjob.tracker.core.dispatcher.storage;

import org.limbo.flowjob.tracker.core.job.context.JobInstance;

/**
 * 作业贮藏所
 *
 * 1. 内存阻塞队列（磁盘）  起一个线程 监听 有任务就消费
 * 2. redis 生产消费
 * 3. DB 存 db后 消费者定时从DB获取
 *
 *
 * 延迟是必然的，你没办法做到 比如 3:00:00 的任务 在3:00:00下发到worker执行
 * 正常的操作来说，如果任务执行和时间有关的话，那应该是 将时间段作为参数传递过去，比如绩效的时候，我算哪天的
 * 每秒一次的任务，也是如此。有两种方式：
 * 1. 传递时间参数，比如 3：00：00 处理这一秒的数据
 * 2. worker 获取当前时间来执行任务（这种就可能会有重复执行的情况，比如任务堆积或者任务排序后导致同时下发，那两个worker获取时间相同）
 *
 * @author Devil
 * @date 2021/7/17 10:47 下午
 */
public interface JobStorage {

    /**
     * 存储一个 job 实例
     * @param instance 实例
     */
    void store(JobInstance instance);

    /**
     * 取走一个 job 实例
     * @return 实例
     */
    JobInstance take();

    /**
     * 修改一个 job 的优先级
     * @param id
     * @param priority
     */
    void adjustPriority(String id, int priority);

}
