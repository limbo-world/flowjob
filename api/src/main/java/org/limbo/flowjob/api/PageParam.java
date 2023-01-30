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

package org.limbo.flowjob.api;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brozen
 * @date 2020/3/6 8:35 AM
 * @email brozen@qq.com
 */
@Data
public class PageParam {
    /**
     * 页码，从1开始
     */
    @Positive(message = "页码不可为负数")
    @Min(value = 1, message = "页码从1开始")
    @Parameter(description = "页码")
    private int current = 1;

    /**
     * 每页条数
     */
    @Positive(message = "条数不可为负数")
    @Max(value = 1000, message = "每页最多1000条数据")
    @Parameter(description = "每页条数，默认20，上限1000")
    private int size = 20;

    /**
     * 最大条数
     */
    public static final int MAX_SIZE = 1000;

    /**
     * 排序字段
     */
    @Parameter(description = "排序字段 和排序方式保持统一长度")
    private List<String> orderBy;

    /**
     * 排序字段的排序方式
     */
    @Parameter(description = "排序方式, 和排序字段保持统一长度")
    private List<String> sort;

    /**
     * 是否查询所有数据
     */
    @Parameter(description = "是否查询所有数据")
    private Boolean needAll;

    /**
     * 是否查询总数
     */
    @Parameter(description = "是否查询总数")
    private boolean searchCount = true;

    /**
     * 获取分页查询的偏移条数
     */
    public int getOffset() {
        return size * (current - 1);
    }

    public List<OrderItem> getOrders() {
        List<OrderItem> orders = new ArrayList<>();
        if (CollectionUtils.isEmpty(orderBy)) {
            return orders;
        }
        for (int i = 0; i < orderBy.size(); i++) {
            if (CollectionUtils.isNotEmpty(sort) && i < sort.size()) {
                orders.add(new OrderItem(orderBy.get(i), sort.get(i)));
            } else {
                orders.add(new OrderItem(orderBy.get(i)));
            }
        }
        return orders;
    }
}
