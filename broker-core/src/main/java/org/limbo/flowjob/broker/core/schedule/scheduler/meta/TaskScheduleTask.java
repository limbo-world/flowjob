package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.strategy.IScheduleStrategy;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author pengqi
 * @date 2023/1/9
 */
public class TaskScheduleTask implements MetaTask {

    @Getter
    private final Task task;

    /**
     * 期望的触发时间
     */
    @Getter
    private final LocalDateTime triggerAt;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final IScheduleStrategy iScheduleStrategy;

    public TaskScheduleTask(Task task, LocalDateTime triggerAt, IScheduleStrategy iScheduleStrategy) {
        this.task = task;
        this.triggerAt = triggerAt;
        this.iScheduleStrategy = iScheduleStrategy;
    }

    @Override
    public void execute() {
        iScheduleStrategy.schedule(task);
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.TASK;
    }

    @Override
    public String getMetaId() {
        return task.getTaskId();
    }

    @Override
    public LocalDateTime scheduleAt() {
        return triggerAt;
    }
}
