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
 * @author Brozen
 * @since 2021-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_worker_statistics")
public class WorkerStatisticsPO extends PO {

    private static final long serialVersionUID = 4463926711851672545L;

    /**
     * worker节点ID
     */
    @TableId(type = IdType.INPUT)
    private String workerId;

    /**
     * 作业下发到此worker的次数
     */
    private Long jobDispatchCount;

    /**
     * 最后一次向此worker下发作业成功的时间
     */
    private LocalDateTime latestDispatchTime;

}
