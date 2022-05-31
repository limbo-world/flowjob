package org.limbo.flowjob.tracker.core.tracker.election.rpc.request;

import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;

import java.io.Serializable;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class ScheduleRequest extends RpcRequest<ResponseDTO<Void>> implements Serializable {

    private static final long serialVersionUID = 6009220462586505649L;

    private Schedulable schedulable;

    public Schedulable getSchedulable() {
        return schedulable;
    }

    public void setSchedulable(Schedulable schedulable) {
        this.schedulable = schedulable;
    }
}
