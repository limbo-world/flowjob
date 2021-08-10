package org.limbo.flowjob.tracker.core.tracker.election.rpc;

import lombok.Data;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;

import java.io.Serializable;

/**
 * @author Devil
 * @since 2021/8/9
 */
@Data
public class ScheduleRequest implements Serializable {

    private static final long serialVersionUID = 6009220462586505649L;

    private String type;

    private Schedulable schedulable;

    private String id;
}
