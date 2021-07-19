package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.storage.JobStorage;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * @author Devil
 * @date 2021/7/19 3:32 下午
 */
public class JobDispatchLauncher {

    /**
     * 调度器
     */
    private JobTracker tracker;
    /**
     * 用于生成JobDispatcher
     */
    private JobDispatcherFactory jobDispatcherFactory;

    /**
     * 发射器
     */
    private Thread launcher;

    public JobDispatchLauncher(JobTracker tracker, JobStorage jobStorage) {
        this.tracker = tracker;
        this.jobDispatcherFactory = new JobDispatcherFactory();

        this.launcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        JobInstance jobInstance = jobStorage.take();
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
            }
        });
    }

    public void start() {
        launcher.start();
    }


}
