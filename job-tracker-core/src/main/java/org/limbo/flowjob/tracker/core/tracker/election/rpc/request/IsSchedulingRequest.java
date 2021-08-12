package org.limbo.flowjob.tracker.core.tracker.election.rpc.request;

import org.limbo.flowjob.tracker.commons.dto.ResponseDto;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class IsSchedulingRequest extends RpcRequest<ResponseDto<Boolean>> {

    private static final long serialVersionUID = -4460786503935507786L;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
