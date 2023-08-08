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

package org.limbo.flowjob.broker.application.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.broker.application.support.JpaHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/4/14
 */
@Slf4j
@Service
public class TaskService {


    public PageDTO<TaskDTO> page(TaskQueryParam param) {
        Specification<Object> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("jobInstanceId").as(String.class), param.getJobInstanceId()));
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("startAt")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        // todo rpc 获取
        Page<Object> queryResult = null;
        List<Object> taskEntities = queryResult.getContent();
        PageDTO<TaskDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(taskEntities)) {
            page.setData(taskEntities.stream().map(taskEntity -> {
                TaskDTO dto = new TaskDTO();
                return dto;
            }).collect(Collectors.toList()));
        }
        return page;
    }

}
