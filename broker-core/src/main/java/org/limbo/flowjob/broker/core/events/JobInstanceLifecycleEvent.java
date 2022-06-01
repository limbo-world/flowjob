package org.limbo.flowjob.broker.core.events;

/**
 * 上下文声明周期事件 todo 是否可以删除
 * <ul>
 *     <li><code>STARTED</code> - 上下文启动，正在分发给worker</li>
 *     <li><code>REFUSED</code> - worker拒绝接收上下文</li>
 *     <li><code>ACCEPTED</code> - worker成功接收上下文</li>
 *     <li><code>CLOSED</code> - 上下文被关闭</li>
 * </ul>
 */
public enum JobInstanceLifecycleEvent {

    /**
     * @see Task#startup(Worker)
     */
    STARTED,

    /**
     * @see Task#refuse(Worker)
     */
    REFUSED,

    /**
     * @see Task#accept(Worker)
     */
    ACCEPTED,

    /**
     * @see Task#close()
     */
    CLOSED

}
