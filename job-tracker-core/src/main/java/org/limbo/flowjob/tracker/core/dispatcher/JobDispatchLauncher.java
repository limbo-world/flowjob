package org.limbo.flowjob.tracker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class JobDispatchLauncher {

    private final JobTracker tracker;
    private final JobInstanceStorage jobInstanceStorage;
    private final JobInstanceRepository jobInstanceRepository;
    private final JobDispatcherFactory jobDispatcherFactory;


    public JobDispatchLauncher(JobTracker tracker, JobInstanceStorage jobInstanceStorage, JobInstanceRepository jobInstanceRepository) {
        this.tracker = tracker;
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
                    // todo 对 cpu 内存 队列等处理
                    jobDispatcher.dispatch(jobInstance, tracker.availableWorkers(), JobInstance::startupContext);
                }
            } catch (Exception e) {
                // todo
                e.printStackTrace();
            }
        }).start();
    }


}
