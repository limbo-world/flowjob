package org.limbo.flowjob.common.lb;

import java.util.Map;

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


    /**
     * 获取用于负载均衡的参数。
     */
    Map<String, String> getLBParameters();



}
