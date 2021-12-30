package org.limbo.flowjob.tracker.core.plan.event;

import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.plan.PlanInfo;

import java.time.Instant;

/**
 * 事件对象：PlanInfo下发
 *
 * @author Brozen
 * @since 2021-10-20
 */
public class PlanInfoDispatchEvent extends Event<PlanInfo> {

    private static final long serialVersionUID = -797029692101068279L;

    public PlanInfoDispatchEvent(PlanInfo planInfo) {
        super(planInfo.getPlanId(), planInfo, Instant.now());
    }

}
