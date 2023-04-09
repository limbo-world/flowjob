package org.limbo.flowjob.common.lb;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Collections;
import java.util.Map;

/**
 * @author Brozen
 * @since 2022-12-14
 */
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class RPCInvocation implements Invocation {

    /**
     * 调用接口的 PATH
     */
    private final String path;

    /**
     * 负载均衡参数
     */
    private final Map<String, String> lbParameters;


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getInvokeTargetId() {
        return this.path;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Map<String, String> getLBParameters() {
        return Collections.unmodifiableMap(lbParameters);
    }

}
