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
//package org.limbo.flowjob.tracker.infrastructure.job.converters;
//
//import com.google.common.base.Converter;
//import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
//import org.limbo.flowjob.tracker.core.job.context.JobAttributes;
//import org.limbo.flowjob.tracker.core.job.context.JobInstance;
//import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
//import org.limbo.flowjob.tracker.dao.po.JobExecuteRecordPO;
//import org.limbo.utils.JacksonUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
//import javax.validation.constraints.NotNull;
//
///**
// * todo 这个类有问题
// *
// * @author Brozen
// * @since 2021-06-02
// */
//@Component
//public class JobExecuteRecordPoConverter extends Converter<JobInstance, JobExecuteRecordPO> {
//
//    /**
//     * 上下文repo
//     */
//    @Lazy
//    @Autowired
//    private JobInstanceRepository jobInstanceRepository;
//
//    /**
//     * 将{@link JobInstance}转换为{@link JobExecuteRecordPO}
//     * @param _do {@link JobInstance}领域对象
//     * @return {@link JobExecuteRecordPO}持久化对象
//     */
//    @Override
//    @NotNull
//    protected JobExecuteRecordPO doForward(JobInstance _do) {
//
//        JobExecuteRecordPO po = new JobExecuteRecordPO();
//        po.setRecordId(_do.getJobInstanceId());
//        po.setJobId(_do.getJobId());
//        po.setStatus(_do.getState().status);
//        po.setWorkerId(_do.getWorkerId());
//        po.setAttributes(JacksonUtils.toJSONString(_do.getJobAttributes()));
//
//        return po;
//
//    }
//
//    /**
//     *
//     * 将{@link JobExecuteRecordPO}转换为{@link JobInstance}
//     * @param po {@link JobExecuteRecordPO}持久化对象
//     * @return {@link JobInstance}领域对象
//     */
//    @Override
//    protected JobInstance doBackward(JobExecuteRecordPO po) {
//
//        JobInstance _do = new JobInstance(jobInstanceRepository);
//        _do.setJobInstanceId(po.getRecordId());
//        _do.setJobId(po.getJobId());
//        _do.setState(JobScheduleStatus.parse(po.getStatus()));
//        _do.setWorkerId(po.getWorkerId());
//        _do.setJobAttributes(JacksonUtils.parseObject(po.getAttributes(), JobAttributes.class));
//
//        return _do;
//
//    }
//
//}
