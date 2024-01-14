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
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.JobInstanceDTO;
import org.limbo.flowjob.api.param.console.JobInstanceQueryParam;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
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
public class JobInstanceAppService {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    public PageDTO<JobInstanceDTO> page(JobInstanceQueryParam param) {
        Specification<JobInstanceEntity> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("planInstanceId").as(String.class), param.getPlanInstanceId()));
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("triggerAt")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        Page<JobInstanceEntity> queryResult = jobInstanceEntityRepo.findAll(sf, pageable);
        List<JobInstanceEntity> jobInstanceEntities = queryResult.getContent();
        PageDTO<JobInstanceDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(jobInstanceEntities)) {
            page.setData(jobInstanceEntities.stream().map(jobInstanceEntity -> {
                JobInstanceDTO dto = new JobInstanceDTO();
                dto.setJobInstanceId(jobInstanceEntity.getJobInstanceId());
                dto.setInstanceId(jobInstanceEntity.getInstanceId());
                dto.setJobId(jobInstanceEntity.getJobId());
                dto.setStatus(jobInstanceEntity.getStatus());
                dto.setRetryTimes(jobInstanceEntity.getRetryTimes());
                dto.setErrorMsg(jobInstanceEntity.getErrorMsg());
                dto.setContext(jobInstanceEntity.getContext());
                dto.setTriggerAt(TimeUtils.toTimestamp(jobInstanceEntity.getTriggerAt()));
                dto.setStartAt(TimeUtils.toTimestamp(jobInstanceEntity.getStartAt()));
                dto.setEndAt(TimeUtils.toTimestamp(jobInstanceEntity.getEndAt()));
                return dto;
            }).collect(Collectors.toList()));
        }
        return page;
    }

}
