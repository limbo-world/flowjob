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

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-17
 */
@Data
@TableName("job")
public class JobPO implements Serializable {

    private static final long serialVersionUID = 3343186004952320736L;

    /**
     * DB自增序列ID，并不是Job的唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long serialId;

    /**
     * 作业唯一标识ID
     */
    private String jobId;

    /**
     * 作业描述
     */
    private String jobDesc;

    /**
     * 作业分发方式
     */
    private Byte dispatchType;

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。
     */
    private Float cpuRequirement;

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。
     */
    private float ramRequirement;

    /**
     * 作业调度方式
     */
    private Byte scheduleType;

    /**
     * 作业延迟时间，单位毫秒
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
