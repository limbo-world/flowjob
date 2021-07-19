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

package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业执行计划
 *
 * @author Brozen
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan")
public class PlanPO extends PO {

    private static final long serialVersionUID = -1639602897831847418L;

    /**
     * DB自增序列ID，并不是唯一标识
     */
    private Long serialId;

    /**
     * 作业执行计划ID
     */
    @TableId(type = IdType.INPUT)
    private String planId;

    /**
     * 执行计划描述
     */
    private String planDesc;

    /**
     * 计划作业分发方式
     */
    private Byte dispatchType;

    /**
     * 计划作业调度方式
     */
    private Byte scheduleType;

    // todo state

    /**
     * 从何时开始调度作业
     */
    private LocalDateTime scheduleStartAt;

    /**
     * 作业调度延迟时间，单位毫秒
     */
    private Long scheduleDelay;

    /**
     * 作业调度间隔时间，单位毫秒。
     */
    private Long scheduleInterval;

    /**
     * 作业调度的CRON表达式
     */
    private String scheduleCron;

    /**
     * 是否启动 新建plan的时候 默认为不启动
     * 接口调用的时候会修改 leader 内存数据以及 db数据 需要保障一致性
     */
    private Boolean isEnabled;
}
