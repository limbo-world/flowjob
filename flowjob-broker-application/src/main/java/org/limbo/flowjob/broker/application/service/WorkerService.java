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
import org.limbo.flowjob.api.param.console.WorkerQueryParam;
import org.limbo.flowjob.api.dto.console.WorkerDTO;
import org.limbo.flowjob.api.dto.broker.WorkerRegisterDTO;
import org.limbo.flowjob.api.param.broker.WorkerHeartbeatParam;
import org.limbo.flowjob.api.param.broker.WorkerRegisterParam;
import org.limbo.flowjob.broker.application.converter.WorkerConverter;
import org.limbo.flowjob.broker.application.support.JpaHelper;
import org.limbo.flowjob.broker.application.support.WorkerFactory;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用层服务
 *
 * @author Brozen
 * @since 2021-07-06
 */
@Slf4j
@Service
public class WorkerService {

    @Setter(onMethod_ = @Inject)
    private WorkerRepository workerRepository;

    @Setter(onMethod_ = @Inject)
    private WorkerEntityRepo workerEntityRepo;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    /**
     * worker注册
     * @param options 注册参数
     * @return 返回所有tracker节点信息
     */
    @Transactional(rollbackOn = Throwable.class)
    public WorkerRegisterDTO register(WorkerRegisterParam options) {
        // TODO ??? 租户鉴权

        // 校验 protocol
        String protocolName = options.getUrl().getProtocol();
        Protocol protocol = Protocol.parse(protocolName);
        Verifies.verify(
                protocol != Protocol.UNKNOWN,
                MsgConstants.UNKNOWN + " worker rpc protocol:" + protocolName
        );

        // 新增 or 更新 worker
        Worker worker = Optional
                .ofNullable(workerRepository.getByName(options.getName()))
                .orElseGet(() -> WorkerFactory.newWorker(idGenerator.generateId(IDType.WORKER), options));

        // 更新注册信息
        worker.register(
                options.getUrl(),
                WorkerConverter.toWorkerTags(options),
                WorkerConverter.toWorkerExecutors(options),
                WorkerConverter.toWorkerMetric(options)
        );

        // 保存 worker
        workerRepository.save(worker);
        log.info("worker registered " + worker);

        return WorkerConverter.toRegisterDTO(worker, nodeManger.allAlive());
    }

    /**
     * worker心跳
     * @param option 心跳参数，上报部分指标数据
     */
    @Transactional(rollbackOn = Throwable.class)
    public WorkerRegisterDTO heartbeat(String workerId, WorkerHeartbeatParam option) {
        // 查询worker并校验
        Worker worker = workerRepository.get(workerId);
        Verifies.requireNotNull(worker, "worker不存在！");

        // 更新metric
        worker.heartbeat(WorkerConverter.toWorkerMetric(option));
        workerRepository.saveMetric(worker);

        if (log.isDebugEnabled()) {
            log.debug("receive heartbeat from " + workerId);
        }

        return WorkerConverter.toRegisterDTO(worker, nodeManger.allAlive());
    }

    public PageDTO<WorkerDTO> page(WorkerQueryParam param) {
        Specification<WorkerEntity> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            if (StringUtils.isNotBlank(param.getName())) {
                Predicate p3 = cb.like(root.get("name").as(String.class), param.getName() + "%");
                p.add(p3);
            }
            p.add(cb.equal(root.get("deleted"),false));
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("workerId")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        Page<WorkerEntity> queryResult = workerEntityRepo.findAll(sf, pageable);
        List<WorkerEntity> entities = queryResult.getContent();
        PageDTO<WorkerDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(entities)) {
            page.setData(entities.stream()
                    .map(WorkerConverter::toVO)
                    .collect(Collectors.toList())
            );
        }
        return page;
    }



}
