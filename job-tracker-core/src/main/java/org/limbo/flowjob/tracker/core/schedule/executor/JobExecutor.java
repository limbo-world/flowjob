///*
// * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.limbo.flowjob.tracker.core.schedule.executor;
//
//import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchService;
//import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchServiceFactory;
//import org.limbo.flowjob.tracker.core.job.context.JobInstance;
//import org.limbo.flowjob.tracker.core.tracker.JobTracker;
//
///**
// * TODO
// * @author Brozen
// * @since 2021-07-13
// */
//public class JobExecutor implements Executor<JobInstance> {
//
//
//    /**
//     * 全局唯一的JobTracker
//     */
//    private JobTracker jobTracker;
//
//    /**
//     * 作业执行器服务
//     */
//    private JobDispatchServiceFactory jobDispatchServiceFactory;
//
//    /**
//     * 通过JobTracker和JobExecutorService构造一个作业执行器。
//     * @param jobTracker JobTracker，进程内唯一
//     * @param jobDispatchServiceFactory 作业执行器工厂
//     */
//    public JobExecutor(JobTracker jobTracker, JobDispatchServiceFactory jobDispatchServiceFactory) {
//        this.jobTracker = jobTracker;
//        this.jobDispatchServiceFactory = jobDispatchServiceFactory;
//    }
//
//    @Override
//    public void execute(JobInstance instance) {
//
//        // 生成新的上下文，并交给执行器执行
//        JobDispatchService dispatchService = jobDispatchServiceFactory.newDispatchService(instance);
//        dispatchService.dispatch(jobTracker, instance);
//
//    }
//
//}
