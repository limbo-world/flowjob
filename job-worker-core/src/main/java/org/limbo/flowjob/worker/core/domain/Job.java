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

/**
 * @author Devil
 * @since 2021/7/24
 */
public class Job {

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 计划实例的ID
     */
    private Long planInstanceId;

    /**
     * 作业ID planId + planInstanceId + jobId 全局唯一
     */
    private String jobId;
    /**
     * 执行器的名称
     */
    private String executorName;
    /**
     * 执行时候的参数
     */
    private String executorParam;

    public String getId() {
        return planId + "-" + planInstanceId + "-" + jobId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Long getPlanInstanceId() {
        return planInstanceId;
    }

    public void setPlanInstanceId(Long planInstanceId) {
        this.planInstanceId = planInstanceId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getExecutorParam() {
        return executorParam;
    }

    public void setExecutorParam(String executorParam) {
        this.executorParam = executorParam;
    }
}
