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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.DelayInstanceDTO;
import org.limbo.flowjob.api.param.console.DelayInstanceQueryParam;
import org.limbo.flowjob.broker.dao.entity.DelayInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.DelayInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.support.JpaHelper;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
public class DelayInstanceAppService {

    @Setter(onMethod_ = @Inject)
    private DelayInstanceEntityRepo delayInstanceEntityRepo;

    public PageDTO<DelayInstanceDTO> page(DelayInstanceQueryParam param) {
        Specification<DelayInstanceEntity> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            if (StringUtils.isNotBlank(param.getBizType())) {
                p.add(cb.equal(root.get("bizType").as(String.class), param.getBizType()));
            }
            if (StringUtils.isNotBlank(param.getBizId())) {
                p.add(cb.equal(root.get("bizId").as(String.class), param.getBizId()));
            }
            if (StringUtils.isNotBlank(param.getTriggerAtBegin()) && StringUtils.isNotBlank(param.getTriggerAtEnd())) {
                p.add(cb.greaterThanOrEqualTo(root.get("triggerAt").as(String.class), param.getTriggerAtBegin()));
                p.add(cb.lessThanOrEqualTo(root.get("triggerAt").as(String.class), param.getTriggerAtEnd()));
            }
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("triggerAt")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        Page<DelayInstanceEntity> queryResult = delayInstanceEntityRepo.findAll(sf, pageable);
        List<DelayInstanceEntity> instanceEntities = queryResult.getContent();
        PageDTO<DelayInstanceDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(instanceEntities)) {
            page.setData(instanceEntities.stream().map(e -> {
                DelayInstanceDTO dto = new DelayInstanceDTO();
                dto.setInstanceId(e.getInstanceId());
                dto.setBizType(e.getBizType());
                dto.setBizId(e.getBizId());
                dto.setStatus(e.getStatus());
                dto.setTriggerAt(TimeUtils.toTimestamp(e.getTriggerAt()));
                dto.setStartAt(TimeUtils.toTimestamp(e.getStartAt()));
                dto.setFeedbackAt(TimeUtils.toTimestamp(e.getFeedbackAt()));
                return dto;
            }).collect(Collectors.toList()));
        }
        return page;
    }

}
