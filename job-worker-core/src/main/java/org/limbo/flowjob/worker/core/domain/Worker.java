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

import org.limbo.flowjob.tracker.commons.dto.worker.WorkerExecutorRegisterDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerResourceDto;
import org.limbo.flowjob.worker.core.infrastructure.AbstractRemoteClient;
import org.limbo.flowjob.worker.core.infrastructure.JobExecutor;
import org.limbo.flowjob.worker.core.infrastructure.JobExecutorRunner;
import org.limbo.flowjob.worker.core.infrastructure.JobManager;
import org.limbo.utils.UUIDUtils;
import org.limbo.utils.verifies.Verifies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作节点实例
 *
 * @author Devil
 * @date 2021/6/10 5:32 下午
 */
public class Worker {
    /**
     * id
     */
    private String id;

    private String ip;

    private int port;
    /**
     * 工作节点资源
     */
    private WorkerResource resource;
    /**
     * 执行器名称 - 执行器 映射关系
     */
    private final Map<String, JobExecutor> executors;
    /**
     * job 管理中心
     */
    private final JobManager jobManager;

    public Worker(String ip, int port, int queueSize, List<JobExecutor> executors) throws Exception {
        this.id = UUIDUtils.randomID();
        this.ip = ip;
        this.port = port;
        this.resource = WorkerResource.create(queueSize);
        this.executors = new ConcurrentHashMap<>();
        this.jobManager = new JobManager();

        Verifies.notEmpty(executors, "empty executors");

        for (JobExecutor executor : executors) {
            Verifies.notBlank(executor.getName(), "has blank executor name");
            this.executors.put(executor.getName(), executor);
        }
    }

    /**
     * 向tracker注册
     */
    public WorkerRegisterOptionDto register() {
        WorkerResourceDto resourceDto = new WorkerResourceDto();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueSize());

        WorkerRegisterOptionDto registerOptionDto = new WorkerRegisterOptionDto();
        registerOptionDto.setId(id);
        registerOptionDto.setIp(ip);
        registerOptionDto.setPort(port);
        // 执行器
        List<WorkerExecutorRegisterDto> workerExecutors = new ArrayList<>();
        for (JobExecutor executor : executors.values()) {
            WorkerExecutorRegisterDto workerExecutorRegisterDto = new WorkerExecutorRegisterDto();
            workerExecutorRegisterDto.setName(executor.getName());
            workerExecutorRegisterDto.setDescription(executor.getDescription());
            workerExecutorRegisterDto.setExecuteType(executor.getType());
            workerExecutors.add(workerExecutorRegisterDto);
        }
        registerOptionDto.setJobExecutors(workerExecutors);
        registerOptionDto.setAvailableResource(resourceDto);
        return registerOptionDto;
    }

    /**
     * 发送心跳
     */
    public WorkerHeartbeatOptionDto heartbeat() {
        WorkerResourceDto resourceDto = new WorkerResourceDto();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueSize());

        WorkerHeartbeatOptionDto heartbeatOptionDto = new WorkerHeartbeatOptionDto();
        heartbeatOptionDto.setWorkerId(id);
        heartbeatOptionDto.setAvailableResource(resourceDto);
        return heartbeatOptionDto;
    }

    /**
     * 提交任务
     */
    public synchronized void receive(Job job) {
        Verifies.verify(executors.containsKey(job.getExecutorName()), "worker doesn't " + job.getExecutorName() + " executor");
        Verifies.verify(jobManager.size() < resource.getAvailableQueueSize(), "worker's queue is full");
        // todo 是否超过cpu/ram/queue 失败

        JobExecutor jobExecutor = executors.get(job.getExecutorName());

        JobExecutorRunner runner = new JobExecutorRunner(jobManager, jobExecutor);

        runner.run(job);
    }

    /**
     * 查询任务状态
     */
    public void jobState(String jobId) {
        if (jobManager.hasJob(jobId)) {

        }
    }

    /**
     * 扩缩容队列
     */
    public void resize(int queueSize) {
        resource.resize(queueSize);
    }

}
