package org.limbo.flowjob.broker.cluster.election.rpc.request;

import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.core.schedule.Scheduled;

import java.io.Serializable;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class ScheduleRequest extends RpcRequest<ResponseDTO<Void>> implements Serializable {

    private static final long serialVersionUID = 6009220462586505649L;

    private Scheduled scheduled;

    public Scheduled getSchedulable() {
        return scheduled;
    }

    public void setSchedulable(Scheduled scheduled) {
        this.scheduled = scheduled;
    }
}
