package org.limbo.flowjob.tracker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class JobDispatchLauncher {

    private final WorkerManager workerManager;
    private final JobInstanceStorage jobInstanceStorage;
    private final JobInstanceRepository jobInstanceRepository;
    private final JobDispatcherFactory jobDispatcherFactory;


    public JobDispatchLauncher(WorkerManager workerManager, JobInstanceStorage jobInstanceStorage,
                               JobInstanceRepository jobInstanceRepository) {
        this.workerManager = workerManager;
        this.jobInstanceStorage = jobInstanceStorage;
        this.jobInstanceRepository = jobInstanceRepository;
        this.jobDispatcherFactory = new JobDispatcherFactory();
    }

    public void start() {
        new Thread(() -> {
            try {
                log.info("JobDispatchLauncher started");
                while (true) {
                    JobInstance jobInstance = jobInstanceStorage.take();
                    // 保存数据
                    jobInstanceRepository.addInstance(jobInstance);

                    JobDispatcher jobDispatcher = jobDispatcherFactory.newDispatcher(jobInstance.getDispatchOption().getDispatchType());
                    if (jobDispatcher == null) {
                        throw new JobExecuteException(jobInstance.getJobId(),
                                "Cannot create JobDispatcher for dispatch type: " + jobInstance.getDispatchOption().getDispatchType());
                    }

                    // 订阅下发成功
                    jobInstance.onContextAccepted().subscribe(instance -> {
                        if (log.isDebugEnabled()) {
                            log.debug(instance.getWorkerId() + " accepted " + instance.getId());
                        }
                        // 由于无法确定先接收到任务执行成功还是任务接收成功的消息，所以可能任务先被直接执行完成了，所以这里需要cas处理
                        jobInstanceRepository.compareAndSwapInstanceState(instance.getPlanId(), instance.getPlanInstanceId(),
                                instance.getJobId(), JobScheduleStatus.Scheduling, instance.getState());
                    });
                    // 订阅下发拒绝
                    jobInstance.onContextRefused().subscribe(instance -> {
                        if (log.isDebugEnabled()) {
                            log.debug(instance.getWorkerId() + " refused " + instance.getId());
                        }
                        jobInstanceRepository.compareAndSwapInstanceState(instance.getPlanId(), instance.getPlanInstanceId(),
                                instance.getJobId(), JobScheduleStatus.Scheduling, instance.getState());
                    });

                    // 下发任务
                    jobDispatcher.dispatch(jobInstance, workerManager.availableWorkers(), JobInstance::startupContext);

                }
            } catch (Exception e) {
                // todo
                e.printStackTrace();
            }
        }).start();
    }


}
