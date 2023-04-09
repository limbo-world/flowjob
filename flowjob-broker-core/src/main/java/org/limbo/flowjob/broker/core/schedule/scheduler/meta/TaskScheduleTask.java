package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskScheduleStrategy;

import java.time.LocalDateTime;

/**
 * @author pengqi
 * @date 2023/1/9
 */
@Slf4j
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
    private final ITaskScheduleStrategy iScheduleStrategy;

    public TaskScheduleTask(Task task, LocalDateTime triggerAt, ITaskScheduleStrategy iScheduleStrategy) {
        this.task = task;
        this.triggerAt = triggerAt;
        this.iScheduleStrategy = iScheduleStrategy;
    }

    @Override
    public void execute() {
        try {
            iScheduleStrategy.schedule(task);
        } catch (Exception e) {
            log.error("task {} schedule fail", task.getTaskId(), e);
        }
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
