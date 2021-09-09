package org.limbo.flowjob.tracker.infrastructure.job.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.TaskResult;
import org.limbo.flowjob.tracker.commons.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.Attributes;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;
import org.limbo.flowjob.tracker.dao.po.TaskPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class TaskPoConverter extends Converter<Task, TaskPO> {

    @Autowired
    private PlanRecordRepository planRecordRepository;

    @Override
    protected TaskPO doForward(Task task) {
        TaskPO po = new TaskPO();
        po.setPlanId(task.getPlanId());
        po.setPlanRecordId(task.getPlanRecordId());
        po.setPlanInstanceId(task.getPlanInstanceId());
        po.setJobId(task.getJobId());
        po.setJobInstanceId(task.getJobInstanceId());
        po.setTaskId(task.getTaskId());
        po.setState(task.getState().status);
        po.setResult(task.getResult().result);
        po.setWorkerId(task.getWorkerId());
        po.setAttributes(task.getAttributes().toString());
        po.setErrorMsg(task.getErrorMsg());
        po.setErrorStackTrace(task.getErrorStackTrace());
        po.setStartAt(TimeUtil.toLocalDateTime(task.getStartAt()));
        po.setEndAt(TimeUtil.toLocalDateTime(task.getEndAt()));
        return po;
    }

    @Override
    protected Task doBackward(TaskPO po) {
        PlanRecord planRecord = planRecordRepository.get(po.getPlanId(), po.getPlanRecordId());
        Job job = planRecord.getDag().getJob(po.getJobId());

        Task task = new Task();
        task.setPlanId(po.getPlanId());
        task.setPlanRecordId(po.getPlanRecordId());
        task.setPlanInstanceId(po.getPlanInstanceId());
        task.setJobId(po.getJobId());
        task.setJobInstanceId(po.getJobInstanceId());
        task.setTaskId(po.getTaskId());
        task.setState(TaskScheduleStatus.parse(po.getState()));
        task.setResult(TaskResult.parse(po.getResult()));
        task.setDispatchOption(job.getDispatchOption());
        task.setExecutorOption(job.getExecutorOption());
        task.setWorkerId(po.getWorkerId());
        task.setType(po.getType());
        task.setAttributes(new Attributes(po.getAttributes()));
        task.setErrorMsg(po.getErrorMsg());
        task.setErrorStackTrace(po.getErrorStackTrace());
        task.setStartAt(TimeUtil.toInstant(po.getStartAt()));
        task.setEndAt(TimeUtil.toInstant(po.getEndAt()));
        return task;
    }

}
