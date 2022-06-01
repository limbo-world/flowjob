package org.limbo.flowjob.broker.ha.election.rpc.request;

import org.limbo.flowjob.broker.api.dto.ResponseDTO;

import java.io.Serializable;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class UnscheduleRequest extends RpcRequest<ResponseDTO<Void>> implements Serializable {

    private static final long serialVersionUID = -7001699540345175235L;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
