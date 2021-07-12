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

/**
 * @author Brozen
 * @since 2021-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_execute_record")
public class JobExecuteRecordPO extends PO {

    private static final long serialVersionUID = -4643515996850585074L;

    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Long serialId;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 执行记录ID
     */
    private String recordId;

    /**
     * 执行状态
     */
    private Byte status;

    /**
     * 执行作业的worker ID
     */
    private String workerId;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;

}
