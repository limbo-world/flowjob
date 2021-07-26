/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.plan;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.core.job.Job;

import java.util.List;

/**
 * 计划实例
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class PlanInstance {

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 计划的版本
     */
    private Integer version;

    /**
     * 从 1 开始增加 planId + version + planInstanceId 全局唯一
     */
    private Long planInstanceId;

    /**
     * 状态
     */
    private PlanScheduleStatus state;

    /**
     * 此执行计划对应的所有作业
     */
    private List<Job> jobs;

    /**
     * 获取最先需要执行的job 因为 DAG 可能会有多个一起执行 todo
     * @return 最先执行的 job
     */
    public List<Job> getEarliestJobs() {
        return jobs;
    }

}
