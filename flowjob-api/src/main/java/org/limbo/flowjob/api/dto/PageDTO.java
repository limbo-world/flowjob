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

package org.limbo.flowjob.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.api.param.PageParam;

import java.util.Collections;
import java.util.List;

/**
 * @author Brozen
 * @date 2020/3/6 8:35 AM
 * @email brozen@qq.com
 */
@Data
public class PageDTO<T> {
    /**
     * 页码
     */
    @Schema(description = "页码")
    private int current = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数")
    private int size = 20;

    /**
     * 总条数
     */
    @Schema(description = "总条数")
    private long total = 0;

    /**
     * 当前页数据
     */
    @Schema(description = "当前页数据")
    private List<T> data;

    /**
     * 是否查询所有数据
     */
    @Schema(description = "是否查询所有数据")
    private Boolean needAll;

    /**
     * 是否还有下一页
     */
    @Schema(description = "是否还有下一页")
    public Boolean getHasNext() {
        return total > current * size;
    }

    /**
     * 获取分页查询的偏移条数
     */
    public int getOffset() {
        return size * (current - 1);
    }

    /**
     * 得到下一页分页对象
     */
    public PageDTO<T> next() {
        PageDTO<T> next = new PageDTO<>();
        next.current = this.current + 1;
        next.size = this.size;
        next.total = this.total;
        return next;
    }

    public List<T> getData() {
        return data == null ? Collections.emptyList() : data;
    }

    public static <V> PageDTO<V> convertByPage(PageParam page) {
        PageDTO<V> result = new PageDTO<>();
        result.setNeedAll(page.getNeedAll());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }
}
