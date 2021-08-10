package org.limbo.flowjob.tracker.core.tracker.election.rpc;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Devil
 * @since 2021/8/10
 */
@Data
public class TrackerNodeRegisterRequest implements Serializable {

    private static final long serialVersionUID = 6804426237962952661L;

    private String ip;

    private int port;

}
