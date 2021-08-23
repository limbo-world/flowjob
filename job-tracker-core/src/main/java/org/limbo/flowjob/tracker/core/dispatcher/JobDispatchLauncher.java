package org.limbo.flowjob.tracker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.schedule.consumer.AcceptedConsumer;
import org.limbo.flowjob.tracker.core.schedule.consumer.RefusedConsumer;
import org.limbo.flowjob.tracker.core.schedule.scheduler.NamedThreadFactory;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;
import org.limbo.utils.verifies.Verifies;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FIXME new线程的方式不够nice
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class JobDispatchLauncher {

    private final WorkerManager workerManager;
    private final JobInstanceStorage jobInstanceStorage;
    private final JobInstanceRepository jobInstanceRepository;
    private final JobDispatcherFactory jobDispatcherFactory;

    /**
     * launcher下发任务并发线程数
     */
    private int concurrency = 1;

    /**
     * 下发任务线程工厂
     */
    private ThreadFactory threadFactory;

    /**
     * 下发任务线程池
     */
    private ExecutorService launchGroup;

    private AtomicBoolean running;

    public JobDispatchLauncher(WorkerManager workerManager,
                               JobInstanceStorage jobInstanceStorage,
                               JobInstanceRepository jobInstanceRepository) {
        this.workerManager = workerManager;
        this.jobInstanceStorage = jobInstanceStorage;
        this.jobInstanceRepository = jobInstanceRepository;
        this.jobDispatcherFactory = new JobDispatcherFactory();
        this.running = new AtomicBoolean(false);
    }


    /**
     * 配置并发launch
     * @param concurrency 并发线程数
     * @param threadFactory 线程工厂
     * @return 链式调用
     */
    public JobDispatchLauncher configConcurrentLaunch(int concurrency, ThreadFactory threadFactory) {
        Verifies.verify(concurrency > 0, "concurrency 必须为正整数：" + concurrency);

        this.concurrency = concurrency;
        this.threadFactory = threadFactory;

        return this;
    }


    /**
     * 配置并发launch。注意会提交<code>concurrency</code>个死循环任务到<code>launchGroup</code>中，需要处理好核心线程数量。
     *
     * @param concurrency 并发线程数
     * @param launchGroup 线程池
     * @return 链式调用
     */
    public JobDispatchLauncher configConcurrentLaunch(int concurrency, ExecutorService launchGroup) {
        Verifies.verify(concurrency > 0, "concurrency 必须为正整数：" + concurrency);

        this.concurrency = concurrency;
        this.launchGroup = launchGroup;

        return this;
    }



    /**
     * lazy初始化消费线程池
     */
    private synchronized void initConsumerGroup(int concurrency, ThreadFactory tf) {
        if (launchGroup != null) {
            return;
        }

        if (tf == null) {
            tf = NamedThreadFactory.newInstance("JobDispatcherLauncher");
        }

        // 提交的任务数量是确定的，因此这里设置没有缓存队列，失败直接抛异常
        launchGroup = new ThreadPoolExecutor(concurrency, concurrency, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), tf, new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * 启动launcher
     */
    @PostConstruct
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        // concurrency需要传递给线程池，还需要作为for循环条件，
        // 防止传入线程池和for循环读取到的值不同，先读到本地变量中
        int concurrency = this.concurrency;
        ThreadFactory tf = this.threadFactory;

        // 初始化线程池
        initConsumerGroup(concurrency, tf);

        // 提交任务
        for (int i = 0; i < concurrency; i++) {
            launchGroup.submit(LaunchTask::new);
        }
    }


    /**
     * 停止launcher
     */
    @PreDestroy
    public void stop() {
        if (running.compareAndSet(true, false)) {
            launchGroup.shutdown();
            launchGroup = null;
        }
    }


    /**
     * launcher任务，在循环中不停的从任务结果中
     */
    private class LaunchTask implements Runnable {

        @Override
        public void run() {
            log.info("JobDispatchLauncher started");
            while (running.get()) {
                try {

                    dispatchJob();

                } catch (JobExecuteException e) {
                    // TODO 异常处理
                    log.error("下发任务异常", e);
                }

            }
        }


        /**
         * 从jobInstanceStorage中读取待下发的任务，并执行下发
         */
        private void dispatchJob() {
            // 从队列消费下发任务，保存数据
            JobInstance jobInstance = jobInstanceStorage.take();
            jobInstanceRepository.addInstance(jobInstance);

            // 初始化dispatcher
            JobDispatcher jobDispatcher = jobDispatcherFactory.newDispatcher(jobInstance.getDispatchOption().getDispatchType());
            if (jobDispatcher == null) {
                throw new JobExecuteException(jobInstance.getJobId(),
                        "Cannot create JobDispatcher for dispatch type: " + jobInstance.getDispatchOption().getDispatchType());
            }

            // 订阅下发成功
            jobInstance.onAccepted().subscribe(new AcceptedConsumer(jobInstanceRepository));
            // 订阅下发拒绝
            jobInstance.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

            // 下发任务
            jobDispatcher.dispatch(jobInstance, workerManager.availableWorkers(), JobInstance::startup);
        }

    }


}
