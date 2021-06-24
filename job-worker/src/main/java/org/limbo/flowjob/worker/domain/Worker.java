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

package org.limbo.flowjob.worker.domain;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.dto.Response;
import org.limbo.flowjob.tracker.commons.dto.job.JobContextDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerResourceDto;
import org.limbo.flowjob.worker.infrastructure.JobExecutor;
import org.limbo.flowjob.worker.infrastructure.JobExecutorRunner;
import org.limbo.flowjob.worker.infrastructure.JobProperties;
import org.limbo.flowjob.worker.infrastructure.JobRunCenter;
import org.limbo.utils.JacksonUtils;
import org.limbo.utils.UUIDUtils;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

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

    private String id;

    private RSocketRequester requester;

    private WorkerResource resource;

    private Map<String, JobExecutor> executors;

    private JobRunCenter jobRunCenter;

    private Worker() {
    }

    public static Worker create(JobProperties config, List<JobExecutor> executors, RSocketStrategies strategies) {
        Worker worker = new Worker();
        worker.id = UUIDUtils.randomID();
        worker.resource = WorkerResource.create(config.getQueueSize());
        worker.executors = new ConcurrentHashMap<>();
        worker.jobRunCenter = new JobRunCenter();

        if (CollectionUtils.isEmpty(executors)) {
            throw new IllegalArgumentException("empty executors");
        }

        for (JobExecutor executor : executors) {
            if (StringUtils.isBlank(executor.getName())) {
                throw new IllegalArgumentException("has blank executor name");
            }
            worker.executors.put(executor.getName(), executor);
        }

        String[] ipHost = config.getTrackerAddress().split(":");

        worker.requester = RSocketRequester.builder()
                .setupRoute("api.worker.connect")
                .rsocketStrategies(strategies)
                .tcp(ipHost[0], Integer.parseInt(ipHost[1]));

        worker.start();

        return worker;
    }

    /**
     * 启动
     */
    public void start() {
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

        requester.route("api.worker.register")
                .data(registerOptionDto)
                .retrieveMono(Response.class)
                .subscribe(response -> System.out.println(JacksonUtils.toJSONString(response)),
                        System.err::println, () -> {
                        });
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

        requester.route("api.worker.heartbeat")
                .data(heartbeatOptionDto)
                .retrieveMono(Response.class)
                .subscribe(response -> System.out.println(JacksonUtils.toJSONString(response)),
                        System.err::println, () -> {
                        });
    }

    /**
     * 提交任务
     */
    public void submit(JobContextDto jobContext) {
        if (!executors.containsKey(jobContext.getExecutor())) {
            // todo 给tracker返回失败
        }

        if (jobRunCenter.size() >= resource.getAvailableQueueSize()) {
            // todo 是否超过cpu/ram/queue 失败
        }


        Job job = new Job();
        job.setId(jobContext.getJobContextId());

        JobExecutor jobExecutor = executors.get(jobContext.getExecutor());

        JobExecutorRunner runner = new JobExecutorRunner(jobRunCenter, jobExecutor);

        runner.run(job);
    }

    /**
     * 查询任务状态
     */
    public void jobState(String jobId) {
        if (jobRunCenter.hasJob(jobId)) {

        }
    }

    public void resize(int queueSize) {
        resource.resize(queueSize);
    }

    public void switchLeader() {

    }

}
