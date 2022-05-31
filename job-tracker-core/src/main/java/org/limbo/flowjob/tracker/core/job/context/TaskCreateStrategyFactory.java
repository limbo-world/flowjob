package org.limbo.flowjob.tracker.core.job.context;

import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.utils.UUIDUtils;

import java.time.Instant;

/**
 * @author Brozen
 * @since 2021-10-20
 */
public class TaskCreateStrategyFactory extends AbstractStrategyFactory<TaskType, TaskCreateStrategyFactory.TaskCreateStrategy, TaskInfo, Task> {

    public TaskCreateStrategyFactory() {
        registerStrategyCreator(TaskType.NORMAL, NormalTaskCreateStrategy::new);
        registerStrategyCreator(TaskType.SHARDING, ShardingTaskCreateStrategy::new);
        registerStrategyCreator(TaskType.BROADCAST, BroadcastTaskCreateStrategy::new);
    }

    /**
     * Task 创建策略接口
     */
    public interface TaskCreateStrategy extends Strategy<TaskInfo, Task> {}


    /**
     * 普通任务创建策略
     */
    public static class NormalTaskCreateStrategy implements TaskCreateStrategy {

        /**
         * 此策略仅适用于 {@link TaskType#NORMAL} 类型的任务
         * @param data 数据
         */
        @Override
        public Boolean canApply(TaskInfo data) {
            return data.getType() == TaskType.NORMAL;
        }


        /**
         * {@inheritDoc}
         * @param info
         * @return
         */
        @Override
        public Task apply(TaskInfo info) {
            Task task = new Task();
            Task.ID taskId = new Task.ID(
                    info.getPlanId(),
                    info.getPlanRecordId(),
                    info.getPlanInstanceId(),
                    info.getJobId(),
                    info.getJobInstanceId(),
                    UUIDUtils.randomID()
            );
            task.setId(taskId);
            task.setState(TaskScheduleStatus.SCHEDULING);
            task.setResult(TaskResult.NONE);
            task.setWorkerId("");
            task.setType(info.getType());
            task.setAttributes(new Attributes());
            task.setDispatchOption(info.getDispatchOption());
            task.setExecutorOption(info.getExecutorOption());
            task.setErrorMsg("");
            task.setErrorStackTrace("");
            task.setStartAt(Instant.EPOCH);
            task.setEndAt(Instant.EPOCH);
            return task;
        }
    }


    /**
     * TODO 分片任务创建策略
     */
    public static class ShardingTaskCreateStrategy implements TaskCreateStrategy {

        @Override
        public Boolean canApply(TaskInfo data) {
            return null;
        }

        @Override
        public Task apply(TaskInfo data) {
            return null;
        }
    }


    /**
     * TODO 广播任务创建策略
     */
    public static class BroadcastTaskCreateStrategy implements TaskCreateStrategy {

        @Override
        public Boolean canApply(TaskInfo data) {
            return null;
        }

        @Override
        public Task apply(TaskInfo data) {
            return null;
        }
    }

}
