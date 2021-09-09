package org.limbo.flowjob.tracker.core.job.consumer;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.context.JobRecordRepository;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.job.context.TaskRepository;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class AcceptedConsumer implements Consumer<Task> {

    private final JobRecordRepository jobRecordRepository;

    private final JobInstanceRepository jobInstanceRepository;

    private final TaskRepository taskRepository;

    public AcceptedConsumer(JobRecordRepository jobRecordRepository, JobInstanceRepository jobInstanceRepository, TaskRepository taskRepository) {
        this.jobRecordRepository = jobRecordRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.taskRepository = taskRepository;
    }


    @Override
    public void accept(Task task) {
        if (log.isDebugEnabled()) {
            log.debug(task.getWorkerId() + " accepted " + task.getId());
        }
        // 由于无法确定先接收到任务执行成功还是任务接收成功的消息，所以可能任务先被直接执行完成了，所以这里需要cas处理
        jobInstanceRepository.executing(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId(), task.getJobInstanceId());
        jobRecordRepository.executing(task.getPlanId(), task.getPlanRecordId(), task.getPlanInstanceId(), task.getJobId());
        taskRepository.executing(task);
    }

}
