/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.domain;

import io.netty.channel.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.dto.job.JobContextDto;
import org.limbo.flowjob.tracker.commons.dto.tcp.JobSubmitResponse;
import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNode;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerResourceDto;
import org.limbo.flowjob.worker.core.infrastructure.JobExecutor;
import org.limbo.flowjob.worker.core.infrastructure.JobExecutorRunner;
import org.limbo.flowjob.worker.core.infrastructure.JobManager;
import org.limbo.utils.UUIDUtils;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Devil
 * @date 2021/6/10 5:32 下午
 */
public class Worker {
    /**
     * id
     */
    private String id;
    /**
     * 工作节点资源
     */
    private WorkerResource resource;
    /**
     * 执行器名称 - 执行器 映射关系
     */
    private Map<String, JobExecutor> executors;
    /**
     * job 管理中心
     */
    private JobManager jobManager;
    /**
     * 所有的tracker
     */
    private List<TrackerNode> trackers;
    /**
     * 当前连接的tracker
     */
    private RemoteClient client;

    public Worker(int queueSize, List<JobExecutor> executors) {
        this.id = UUIDUtils.randomID();
        this.resource = WorkerResource.create(queueSize);
        this.executors = new ConcurrentHashMap<>();
        this.jobManager = new JobManager();

        if (CollectionUtils.isEmpty(executors)) {
            throw new IllegalArgumentException("empty executors");
        }

        for (JobExecutor executor : executors) {
            if (StringUtils.isBlank(executor.getName())) {
                throw new IllegalArgumentException("has blank executor name");
            }
            this.executors.put(executor.getName(), executor);
        }
    }

    /**
     * 启动
     */
    public void start(String host, int port) throws Exception {
        synchronized (client) {
            if (client != null) {
                return;
            }
        }
        // 连接服务端
        this.client = new RemoteClient(host, port, this);
        this.client.start();
        // 注册
        register();
        // 心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                heartBeat();
            }
        }, 200, 3000);
    }

    /**
     * 向tracker注册
     */
    private void register() {
        WorkerResourceDto resourceDto = new WorkerResourceDto();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueSize());

        WorkerRegisterOptionDto registerOptionDto = new WorkerRegisterOptionDto();
        registerOptionDto.setId(id);
        registerOptionDto.setAvailableResource(resourceDto);

        this.client.getChannel().writeAndFlush(registerOptionDto);
    }

    /**
     * 发送心跳
     */
    private void heartBeat() {
        WorkerResourceDto resourceDto = new WorkerResourceDto();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueSize());

        WorkerHeartbeatOptionDto heartbeatOptionDto = new WorkerHeartbeatOptionDto();
        heartbeatOptionDto.setAvailableResource(resourceDto);

        this.client.getChannel().writeAndFlush(heartbeatOptionDto);
    }

    /**
     * 提交任务
     */
    public void submit(String id, String executorName) {
        if (!executors.containsKey(executorName)) {
            JobSubmitResponse<Void> response = new JobSubmitResponse<>(400,
                    "worker don't have this executor name's " + executorName, null);
            client.getChannel().writeAndFlush(response);
            return;
        }

        if (jobManager.size() >= resource.getAvailableQueueSize()) {
            // todo 是否超过cpu/ram/queue 失败
            JobSubmitResponse<Void> response = new JobSubmitResponse<>(400, "", null);
            client.getChannel().writeAndFlush(response);
            return;
        }


        Job job = new Job();
        job.setId(id);

        JobExecutor jobExecutor = executors.get(executorName);

        JobExecutorRunner runner = new JobExecutorRunner(jobManager, jobExecutor);

        runner.run(job);
        JobSubmitResponse<Void> response = new JobSubmitResponse<>(200, "success", null);
        client.getChannel().writeAndFlush(response);
    }

    /**
     * 查询任务状态
     */
    public void jobState(String jobId) {
        if (jobManager.hasJob(jobId)) {

        }
    }

    public void resize(int queueSize) {
        resource.resize(queueSize);
    }

    public void setTrackers(List<TrackerNode> trackers) {
        this.trackers = trackers;
    }

}
