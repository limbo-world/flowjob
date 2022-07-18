package org.limbo.flowjob.broker.core.plan.job.context;

import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.utils.strategies.AbstractStrategyFactory;
import org.limbo.flowjob.broker.core.utils.strategies.Strategy;
import org.limbo.flowjob.common.utils.UUIDUtils;

import java.time.Instant;

/**
 * @author Brozen
 * @since 2021-10-20
 */
public class TaskCreateStrategyFactory extends AbstractStrategyFactory<TaskType, TaskCreateStrategyFactory.TaskCreateStrategy, JobInstance, Task> {

    public TaskCreateStrategyFactory() {
        registerStrategyCreator(TaskType.NORMAL, NormalTaskCreateStrategy::new);
        registerStrategyCreator(TaskType.SHARDING, ShardingTaskCreateStrategy::new);
        registerStrategyCreator(TaskType.BROADCAST, BroadcastTaskCreateStrategy::new);
    }

    /**
     * Task 创建策略接口，在这里对 Task 进行多种代理（装饰），实现下发重试策略。
     * @see DispatchRetryableTask
     */
    public interface TaskCreateStrategy extends Strategy<JobInstance, Task> {}


    /**
     * 普通任务创建策略
     */
    public static class NormalTaskCreateStrategy implements TaskCreateStrategy {

        /**
         * 此策略仅适用于 {@link TaskType#NORMAL} 类型的任务
         * @param job 作业实例
         */
        @Override
        public Boolean canApply(JobInstance job) {
            return job.getTaskType() == TaskType.NORMAL;
        }


        /**
         * {@inheritDoc}
         * @param job
         * @return
         */
        @Override
        public Task apply(JobInstance job) {
            Task task = new Task();
            task.setTaskId(UUIDUtils.randomID()); // TODO taskId如何生成？
            task.setPlanId(job.getPlanId());
            task.setPlanInstanceId(job.getPlanInstanceId());
            task.setJobId(job.getJobId());
            task.setJobInstanceId(job.getJobInstanceId());
            task.setState(TaskScheduleStatus.SCHEDULING);
            task.setResult(TaskResult.NONE);
            task.setWorkerId("");
            task.setType(job.getTaskType());
            task.setAttributes(new Attributes());
            task.setDispatchOption(job.getDispatchOption());
            task.setExecutorOption(job.getExecutorOption());
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
        public Boolean canApply(JobInstance data) {
            return null;
        }

        @Override
        public Task apply(JobInstance data) {
            return null;
        }
    }


    /**
     * TODO 广播任务创建策略
     */
    public static class BroadcastTaskCreateStrategy implements TaskCreateStrategy {

        @Override
        public Boolean canApply(JobInstance data) {
            return null;
        }

        @Override
        public Task apply(JobInstance data) {
            return null;
        }
    }

}
