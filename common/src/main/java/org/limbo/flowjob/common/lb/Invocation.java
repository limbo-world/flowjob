package org.limbo.flowjob.common.lb;

/**
 * @author Brozen
 * @since 2022-12-14
 */
public interface Invocation {

    /**
     * 调用目标的 ID。
     * 对于 RPC，是远程接口的地址；
     * 对于 Job ，可以是任务的名称；
     */
    String getInvokeTargetId();




}
