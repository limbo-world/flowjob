package org.limbo.flowjob.tracker.core.tracker.election;

import com.alipay.sofa.jraft.util.Endpoint;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.RpcCaller;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.request.IsSchedulingRequest;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.request.ScheduleRequest;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.request.UnscheduleRequest;

/**
 * 转发请求 远程调用 leader
 *
 * @author Devil
 * @since 2021/8/9
 */
public class FollowerJobTracker implements JobTracker {

    private Endpoint endpoint;

    private RpcCaller rpcCaller;

    public FollowerJobTracker(Endpoint endpoint, RpcCaller rpcCaller) {
        this.endpoint = endpoint;
        this.rpcCaller = rpcCaller;
    }

    @Override
    public void schedule(Schedulable schedulable) {
        ScheduleRequest request = new ScheduleRequest();
        request.setSchedulable(schedulable);

        // TODO 异常处理
        ResponseDto<Void> response = rpcCaller.invokeSync(endpoint, request);
    }

    @Override
    public void unschedule(String id) {
        UnscheduleRequest request = new UnscheduleRequest();
        request.setId(id);

        // TODO 异常处理
        ResponseDto<Void> response = rpcCaller.invokeSync(endpoint, request);
    }

    @Override
    public boolean isScheduling(String id) {
        IsSchedulingRequest request = new IsSchedulingRequest();
        request.setId(id);

        // TODO 异常处理
        ResponseDto<Boolean> response = rpcCaller.invokeSync(endpoint, request);
        return response.getData();
    }
}
