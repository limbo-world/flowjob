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

package org.limbo.flowjob.api.console.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.limbo.flowjob.common.constants.LoadBalanceType;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业分发配置参数")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchOptionParam {

    /**
     * 负载方式
     */
    @NotNull
    @Schema(title = "负载方式", implementation = Integer.class)
    private LoadBalanceType loadBalanceType;

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     */
    @Schema(title = "所需的CPU核心数", description = "小于等于0表示此作业未定义CPU需求")
    private BigDecimal cpuRequirement;

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     */
    @Schema(title = "所需的内存GB数", description = "小于等于0表示此作业未定义内存需求")
    private BigDecimal ramRequirement;

    @Schema(title = "标签过滤", description = "根据指定标签过滤")
    private List<TagFilterParam> tagFilters;

}
