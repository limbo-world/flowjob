package org.limbo.flowjob.tracker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;
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

                    // 成功 失败 拒绝 不同情况的处理 todo
                    jobInstance.onContextAccepted().subscribe(c -> {
                        System.out.println(c.getWorkerId() + " accepted");
                        jobInstanceRepository.updateInstance(jobInstance);
                    });
                    jobInstance.onContextRefused().subscribe(c -> {
                        System.out.println(c.getWorkerId() + " refused");
                        jobInstanceRepository.updateInstance(jobInstance);
                    });
                    jobInstance.onContextClosed().subscribe(c -> {
                        System.out.println(c.getId() + " closed");
                        jobInstanceRepository.updateInstance(jobInstance);
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
