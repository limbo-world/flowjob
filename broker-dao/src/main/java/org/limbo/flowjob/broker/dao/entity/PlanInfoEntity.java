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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * plan 的信息存档 历史版本
 *
 * @author Brozen
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_plan_info")
public class PlanInfoEntity extends Entity {

    private static final long serialVersionUID = -1639602897831847418L;

    /**
     * 版本 全局唯一
     */
    @TableId(type = IdType.INPUT)
    private String planInfoId;

    /**
     * 属于哪个plan
     */
    private String planId;

    /**
     * 执行计划描述
     */
    private String description;

    /**
     * 计划作业调度方式
     */
    private Byte scheduleType;

    /**
     * 从何时开始调度作业
     */
    private LocalDateTime scheduleStartAt;

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
     * 重试次数 超过执行就失败
     * job上的这个版本不设计了，用户本来就需要做幂等处理
     */
    private Integer retry;

    /**
     * 任务 json string
     */
    private String jobs;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}
