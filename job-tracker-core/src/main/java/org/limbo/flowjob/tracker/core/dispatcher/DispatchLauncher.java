package org.limbo.flowjob.tracker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.Dispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.consumer.AcceptedConsumer;
import org.limbo.flowjob.tracker.core.job.consumer.RefusedConsumer;
import org.limbo.flowjob.tracker.core.job.context.*;
import org.limbo.flowjob.tracker.core.plan.*;
import org.limbo.flowjob.tracker.core.schedule.scheduler.NamedThreadFactory;
import org.limbo.flowjob.tracker.core.storage.Storable;
import org.limbo.flowjob.tracker.core.storage.Storage;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;
import org.limbo.utils.verifies.Verifies;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class DispatchLauncher {

    private final WorkerManager workerManager;
    private final Storage storage;
    private final PlanRecordRepository planRecordRepository;
    private final PlanInstanceRepository planInstanceRepository;
    private final JobRecordRepository jobRecordRepository;
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

    private final AtomicBoolean running;

    private List<DispatchHandler> dispatchHandlers;

    public DispatchLauncher(WorkerManager workerManager,
                            Storage storage,
                            PlanRecordRepository planRecordRepository,
                            PlanInstanceRepository planInstanceRepository,
                            JobRecordRepository jobRecordRepository,
                            JobInstanceRepository jobInstanceRepository) {
        this.workerManager = workerManager;
        this.storage = storage;
        this.planRecordRepository = planRecordRepository;
        this.planInstanceRepository = planInstanceRepository;
        this.jobRecordRepository = jobRecordRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.jobDispatcherFactory = new JobDispatcherFactory();
        this.dispatchHandlers = new ArrayList<>();
        this.running = new AtomicBoolean(false);
    }


    /**
     * 配置并发launch
     *
     * @param concurrency   并发线程数
     * @param threadFactory 线程工厂
     * @return 链式调用
     */
    public DispatchLauncher configConcurrentLaunch(int concurrency, ThreadFactory threadFactory) {
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
    public DispatchLauncher configConcurrentLaunch(int concurrency, ExecutorService launchGroup) {
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

        // 分发处理器
        dispatchHandlers.add(new PlanDispatchHandler());
        dispatchHandlers.add(new PlanRecordDispatchHandler());
        dispatchHandlers.add(new JobRecordDispatchHandler());
        dispatchHandlers.add(new TaskDispatchHandler());

        // 提交任务
        for (int i = 0; i < concurrency; i++) {
            launchGroup.submit(new DispatchTask()); // submit(LaunchTask::new); 会用 LaunchTask 的构造方法代替 call 方法 所以不执行run
        }

        log.info("DispatchLauncher started");
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
    private class DispatchTask implements Runnable {

        @Override
        public void run() {
            while (running.get()) {
                try {
                    // 从队列消费下发任务
                    Storable storable = storage.take();
                    for (DispatchHandler dispatchHandler : dispatchHandlers) {
                        if (dispatchHandler.matched(storable)) {
                            dispatchHandler.dispatch(storable);
                        }
                    }

                } catch (Exception e) {
                    // TODO 异常处理
                    log.error("下发任务异常", e);
                }

            }
        }

    }

    private interface DispatchHandler {
        void dispatch(Storable t);
        boolean matched(Storable t);
    }

    private class PlanDispatchHandler implements DispatchHandler {

        @Override
        public void dispatch(Storable storable) {
            Plan plan = (Plan) storable;
            Long planRecordId = planRecordRepository.createId(plan.getPlanId());
            PlanRecord planRecord = plan.newRecord(planRecordId, PlanScheduleStatus.Scheduling,
                    ScheduleType.FIXED_INTERVAL == plan.getScheduleOption().getScheduleType());
            planRecordRepository.add(planRecord);
            storage.store(planRecord);
        }

        @Override
        public boolean matched(Storable storable) {
            return storable instanceof Plan;
        }
    }

    private class PlanRecordDispatchHandler implements DispatchHandler {

        @Override
        public void dispatch(Storable storable) {
            PlanRecord planRecord = (PlanRecord) storable;
            Long planInstanceId = planInstanceRepository.createId(planRecord.getPlanId(), planRecord.getPlanRecordId());
            PlanInstance planInstance = planRecord.newInstance(planInstanceId, PlanScheduleStatus.Scheduling);
            planInstanceRepository.add(planInstance);

            List<Job> jobs = planInstance.getDag().getEarliestJobs();
            for (Job job : jobs) {
                JobRecord jobRecord = job.newRecord(planInstance.getPlanId(), planInstance.getPlanRecordId(), planInstanceId, JobScheduleStatus.Scheduling);
                jobRecordRepository.add(jobRecord);
                storage.store(jobRecord);
            }
        }

        @Override
        public boolean matched(Storable storable) {
            return storable instanceof PlanRecord;
        }
    }

    private class JobRecordDispatchHandler implements DispatchHandler {

        @Override
        public void dispatch(Storable storable) {
            JobRecord jobRecord = (JobRecord) storable;
            Long jobInstanceId = jobInstanceRepository.createId();
            JobInstance jobInstance = jobRecord.newInstance(jobInstanceId, JobScheduleStatus.Scheduling);
            jobInstanceRepository.add(jobInstance);
            List<Task> tasks = jobInstance.tasks();
            for (Task task : tasks) {
                storage.store(task);
            }
        }

        @Override
        public boolean matched(Storable storable) {
            return storable instanceof JobRecord;
        }
    }

    private class TaskDispatchHandler implements DispatchHandler {

        @Override
        public void dispatch(Storable storable) {
            Task task = (Task) storable;
            // todo 下发前确认下对应的jobInstance是否已经关闭
            // 初始化dispatcher
            Dispatcher dispatcher = jobDispatcherFactory.newDispatcher(task.getDispatchOption().getLoadBalanceType());
            if (dispatcher == null) {
                throw new JobExecuteException(task.getJobId(),
                        "Cannot create JobDispatcher for dispatch type: " + task.getDispatchOption().getLoadBalanceType());
            }

            // 订阅下发成功
            task.onAccepted().subscribe(new AcceptedConsumer(jobInstanceRepository));
            // 订阅下发拒绝
            task.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

            // 下发任务
            dispatcher.dispatch(task, workerManager.availableWorkers(), Task::startup);
        }

        @Override
        public boolean matched(Storable storable) {
            return storable instanceof Task;
        }
    }

}
