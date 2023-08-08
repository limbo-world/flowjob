/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.agent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.agent.constants.AgentStatus;
import org.limbo.flowjob.agent.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.service.JobService;
import org.limbo.flowjob.agent.service.TaskService;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.common.exception.BrokerRpcException;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.heartbeat.Heartbeat;
import org.limbo.flowjob.common.heartbeat.HeartbeatPacemaker;
import org.limbo.flowjob.common.thread.NamedThreadFactory;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
public class BaseScheduleAgent implements ScheduleAgent, Heartbeat {

    /**
     * 通信基础 URL
     */
    @Getter
    private URL url;

    /**
     * 远程调用
     */
    private AgentBrokerRpc brokerRpc;

    /**
     * 并发执行任务数量
     */
    private int concurrency;

    /**
     * 队列数
     */
    private int queueSize;

    /**
     * 任务执行线程池
     */
    private ExecutorService threadPool;

    /**
     * 是否已经启动
     */
    private AtomicReference<AgentStatus> status;

    /**
     * 心跳起搏器
     */
    private HeartbeatPacemaker pacemaker;

    private JobService jobService;

    private TaskService taskService;

    public BaseScheduleAgent(URL url, AgentBrokerRpc brokerRpc) {
        Objects.requireNonNull(url, "URL can't be null");
        Objects.requireNonNull(brokerRpc, "remote client can't be null");

        this.url = url;
        this.brokerRpc = brokerRpc;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public void start(Duration heartbeatPeriod) {
        Objects.requireNonNull(heartbeatPeriod);

        // 重复检测
        if (!status.compareAndSet(AgentStatus.IDLE, AgentStatus.INITIALIZING)) {
            return;
        }

        Heartbeat heartbeat = this;

        Timer startTimer = new Timer();
        TimerTask startTask = new TimerTask() {
            @Override
            public void run() {
                // 状态检测
                if (AgentStatus.INITIALIZING != status.get()) {
                    startTimer.cancel();
                    return;
                }
                // 注册
                try {
                    registerSelfToBroker();
                } catch (Exception e) {
                    log.error("Register to broker has error", e);
                    return;
                }

                // 启动心跳
                if (pacemaker == null) {
                    pacemaker = new HeartbeatPacemaker(heartbeat, Duration.ofSeconds(1));
                }
                pacemaker.start();

                // 初始化线程池
                BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize <= 0 ? concurrency : queueSize);
                threadPool = new ThreadPoolExecutor(
                        concurrency, concurrency,
                        5, TimeUnit.SECONDS, queue,
                        NamedThreadFactory.newInstance("FlowJobAgentExecutor"),
                        (r, e) -> {
                            throw new RejectedExecutionException();
                        }
                );

                // 更新为运行中
                status.compareAndSet(AgentStatus.INITIALIZING, AgentStatus.RUNNING);
                log.info("worker start!");
            }
        };

        startTimer.scheduleAtFixedRate(startTask, 0, 3000);

    }

    @Override
    public void sendHeartbeat() {
        try {
            brokerRpc.heartbeat(this);
        } catch (BrokerRpcException e) {
            log.warn("Agent send heartbeat failed");
            throw new IllegalStateException("Agent send heartbeat failed", e);
        }
    }

    /**
     * 向 Broker 注册当前 Worker
     */
    private void registerSelfToBroker() {
        try {
            // 调用 Broker 远程接口，并更新 Broker 信息
            brokerRpc.register(this);
        } catch (RegisterFailException e) {
            log.error("Worker register failed", e);
            throw e;
        }

        log.info("register success!");
    }

    @Override
    public void receiveJob(Job job) {
        assertRunning();

        // 找到执行器，校验是否存在
        int availableQueueSize = availableQueueSize();
        if (jobService.count() >= availableQueueSize) {
            throw new IllegalArgumentException("Worker's queue is full, limit: " + availableQueueSize);
        }

        jobService.save(job);

        try {
            // 提交执行
            this.threadPool.submit(() -> jobService.schedule(job));
        } catch (RejectedExecutionException e) {
            throw new IllegalStateException("Schedule job failed, maybe thread exhausted");
        }
    }

    @Override
    public void taskSuccess(Task task, Object result) {
        taskService.taskSuccess(task, result);
    }

    @Override
    public void taskFail(Task task, String errorMsg, String errorStackTrace) {
        taskService.taskFail(task, errorMsg, errorStackTrace);
    }

    /**
     * 验证 worker 正在运行中
     */
    private void assertRunning() {
        if (this.status.get() != AgentStatus.RUNNING) {
            throw new IllegalStateException("Agent is not running: " + this.status.get());
        }
    }

    public int availableQueueSize() {
        return queueSize - jobService.count();
    }

    @Override
    public void stop() {
        // todo
    }

    @Override
    public void beat() {
        this.sendHeartbeat();
    }

}
