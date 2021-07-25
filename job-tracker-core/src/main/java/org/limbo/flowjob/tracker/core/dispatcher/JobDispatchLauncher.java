package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Devil
 * @since 2021/7/24
 */
public class JobDispatchLauncher {

    /**
     * 发射器
     */
    private final Thread launcher;

    public JobDispatchLauncher(JobTracker tracker, JobInstanceStorage jobInstanceStorage, JobInstanceRepository jobInstanceRepository) {
        JobDispatcherFactory jobDispatcherFactory = new JobDispatcherFactory();

        this.launcher = new Thread(() -> {
            try {
                while (true) {
                    JobInstance jobInstance = jobInstanceStorage.take();
                    // 保存数据
                    jobInstanceRepository.addInstance(jobInstance);

                    JobDispatcher jobDispatcher = jobDispatcherFactory.newDispatcher(jobInstance.getDispatchType());
                    if (jobDispatcher == null) {
                        throw new JobExecuteException(jobInstance.getJobId(),
                                "Cannot create JobDispatcher for dispatch type: " + jobInstance.getDispatchType());
                    }

                    jobDispatcher.dispatch(jobInstance, tracker.availableWorkers(), JobInstance::startupContext);
                }
            } catch (Exception e) {
                // todo
            }
        });
    }

    public void start() {
        launcher.start();
    }


}
