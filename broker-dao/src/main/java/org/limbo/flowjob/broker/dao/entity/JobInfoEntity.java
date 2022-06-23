/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.limbo.flowjob.broker.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.broker.api.console.param.ExecutorOptionParam;

/**
 * plan 中 每个流程节点的具体信息
 *
 * @author Devil
 * @since 2022/6/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_job_info")
public class JobInfoEntity extends BaseEntity {
    private static final long serialVersionUID = -492555252883668183L;

    /**
     * 全局唯一
     */
    @TableId(type = IdType.INPUT)
    private String jobInfoId;

    private String planInfoId;

    /**
     * 作业名称
     */
    private String name;

    /**
     * 作业描述
     */
    private String description;

    /**
     * 此作业相连的下级作业ID todo 是否需要再加个关联表查询
     */
    private String childrenIds;

    /**
     * 作业分发配置参数
     */
    private DispatchOptionParam dispatchOption;

    /**
     * 执行器配置参数
     */
    private ExecutorOptionParam executorOption;
}
