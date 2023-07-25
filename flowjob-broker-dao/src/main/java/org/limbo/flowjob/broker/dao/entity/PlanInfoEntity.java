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

package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * plan 的信息存档 历史版本
 *
 * @author Brozen
 * @since 2021-07-13
 */
@Setter
@Getter
@Table(name = "flowjob_plan_info")
@Entity
@DynamicInsert
@DynamicUpdate
public class PlanInfoEntity extends BaseEntity {

    private static final long serialVersionUID = -1639602897831847418L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String planInfoId;

    /**
     * 属于哪个plan
     */
    private String planId;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 计划作业类型
     * @see PlanType
     */
    private Integer planType;

    /**
     * 计划作业调度方式
     * @see ScheduleType
     */
    private Integer scheduleType;

    /**
     * 计划作业调度方式
     * @see TriggerType
     */
    private Integer triggerType;

    /**
     * 从何时开始调度作业
     */
    private LocalDateTime scheduleStartAt;

    /**
     * 从何时结束调度作业
     */
    private LocalDateTime scheduleEndAt;

    /**
     * 作业调度延迟时间，单位秒
     */
    private Long scheduleDelay;

    /**
     * 作业调度间隔时间，单位秒。
     */
    private Long scheduleInterval;

    /**
     * 作业调度的CRON表达式
     */
    private String scheduleCron;

    /**
     * 作业调度的CRON表达式
     */
    private String scheduleCronType;

    /**
     * single 存储job信息
     * workflow存储job节点之间的关联关系
     */
    private String jobInfo;

    @Override
    public Object getUid() {
        return planInfoId;
    }
}
