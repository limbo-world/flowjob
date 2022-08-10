package org.limbo.flowjob.broker.core.plan.job.context;

import org.limbo.flowjob.broker.api.constants.enums.JobType;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.common.utils.UUIDUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Brozen
 * @since 2021-10-20
 */
public class TaskCreatorFactory {

    /**
     * 策略类型和策略生成器直接的映射
     */
    private final Map<JobType, TaskCreator> taskCreators;

    public TaskCreatorFactory(WorkerManager workerManager) {
        taskCreators = new EnumMap<>(JobType.class);

        taskCreators.put(JobType.NORMAL, new NormalTaskCreator(workerManager));
        taskCreators.put(JobType.SHARDING, new ShardingTaskCreator(workerManager));
        taskCreators.put(JobType.BROADCAST, new BroadcastTaskCreator(workerManager));
    }

    public TaskCreator get(JobType type) {
        return taskCreators.get(type);
    }

    /**
     * Task 创建策略接口，在这里对 Task 进行多种代理（装饰），实现下发重试策略。
     */
    public abstract class TaskCreator implements Function<JobInstance, List<Task>> {

        protected final WorkerManager workerManager;

        public TaskCreator(WorkerManager workerManager) {
            this.workerManager = workerManager;
        }

        public List<Task> create(JobInstance jobInstance) {
            if (!match(jobInstance)) {
                return Collections.emptyList();
            }
            return apply(jobInstance);
        }

        public boolean match(JobInstance jobInstance) {
            return jobInstance.getType() == getType();
        }

        public abstract JobType getType();


    }


    /**
     * 普通任务创建策略
     */
    public class NormalTaskCreator extends TaskCreator {

        public NormalTaskCreator(WorkerManager workerManager) {
            super(workerManager);
        }

        /**
         * 此策略仅适用于 {@link JobType#NORMAL} 类型的任务
         */
        @Override
        public JobType getType() {
            return JobType.NORMAL;
        }

        /**
         * {@inheritDoc}
         * @param job
         * @return
         */
        @Override
        public List<Task> apply(JobInstance job) {
            Task task = new Task();
            task.setPlanInstanceId(job.getPlanInstanceId());
            task.setJobId(job.getJobId());
            task.setJobInstanceId(job.getJobInstanceId());
            task.setStatus(TaskStatus.DISPATCHING);
            task.setWorkerId("");
            task.setAttributes(new Attributes());
            task.setDispatchOption(job.getDispatchOption());
            task.setExecutorOption(job.getExecutorOption());
            task.setWorkerManager(workerManager);
            task.setErrorMsg("");
            task.setErrorStackTrace("");
            task.setStartAt(Instant.EPOCH);
            task.setEndAt(Instant.EPOCH);
            return Collections.singletonList(task);
        }
    }


    /**
     * TODO 分片任务创建策略
     */
    public class ShardingTaskCreator extends TaskCreator {

        public ShardingTaskCreator(WorkerManager workerManager) {
            super(workerManager);
        }

        @Override
        public List<Task> apply(JobInstance job) {
            return null;
        }

        @Override
        public JobType getType() {
            return JobType.SHARDING;
        }
    }


    /**
     * TODO 广播任务创建策略
     */
    public class BroadcastTaskCreator extends TaskCreator {

        public BroadcastTaskCreator(WorkerManager workerManager) {
            super(workerManager);
        }

        @Override
        public JobType getType() {
            return JobType.BROADCAST;
        }

        @Override
        public List<Task> apply(JobInstance job) {
            return null;
        }
    }

}
