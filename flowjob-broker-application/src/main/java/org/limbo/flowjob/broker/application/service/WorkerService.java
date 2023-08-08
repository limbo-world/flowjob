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
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.constants.WorkerStatus;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.broker.WorkerRegisterDTO;
import org.limbo.flowjob.api.dto.console.WorkerDTO;
import org.limbo.flowjob.api.param.broker.WorkerHeartbeatParam;
import org.limbo.flowjob.api.param.broker.WorkerRegisterParam;
import org.limbo.flowjob.api.param.console.WorkerQueryParam;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.converter.WorkerConverter;
import org.limbo.flowjob.broker.application.support.JpaHelper;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerExecutorEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerMetricEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerSlotEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerTagEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerExecutorEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerMetricEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerSlotEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerTagEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
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
import java.util.Map;
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
    private WorkerEntityRepo workerEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerTagEntityRepo workerTagEntityRepo;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    private SlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private WorkerSlotEntityRepo workerSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerMetricEntityRepo workerMetricEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerExecutorEntityRepo workerExecutorEntityRepo;

    @Setter(onMethod_ = @Inject)
    private BrokerConfig brokerConfig;

    /**
     * worker注册
     *
     * @param options 注册参数
     * @return 返回所有tracker节点信息
     */
    @Transactional(rollbackOn = Throwable.class)
    public WorkerRegisterDTO register(WorkerRegisterParam options) {
        // 校验 protocol
        String protocolName = options.getUrl().getProtocol();
        Protocol protocol = Protocol.parse(protocolName);
        Verifies.verify(
                protocol != Protocol.UNKNOWN,
                MsgConstants.UNKNOWN + " worker rpc protocol:" + protocolName
        );

        // 新增 or 更新 worker
        WorkerEntity workerEntity = workerEntityRepo.findByNameAndDeleted(options.getName(), false).orElse(null);
        if (workerEntity == null) {
            String workerId = idGenerator.generateId(IDType.WORKER);

            workerEntity = new WorkerEntity();
            workerEntity.setAppId("");
            workerEntity.setWorkerId(workerId);
            workerEntity.setName(options.getName());
            workerEntity.setProtocol(options.getUrl().getProtocol());
            workerEntity.setHost(options.getUrl().getHost());
            workerEntity.setPort(options.getUrl().getPort());
            workerEntity.setEnabled(true);

            WorkerSlotEntity slotEntity = new WorkerSlotEntity();
            slotEntity.setSlot(slotManager.slot(workerId));
            slotEntity.setWorkerId(workerId);
            workerSlotEntityRepo.saveAndFlush(slotEntity);
        }

        String workerId = workerEntity.getWorkerId();

        // worker 存储
        workerEntity.setStatus(WorkerStatus.RUNNING.status);
        workerEntity.setUpdatedAt(TimeUtils.currentLocalDateTime());
        workerEntityRepo.saveAndFlush(workerEntity);

        // Metric 存储
        WorkerMetricEntity workerMetricEntity = WorkerConverter.toWorkerMetricEntity(workerId, options.getAvailableResource());
        workerMetricEntityRepo.saveAndFlush(workerMetricEntity);

        // Executors 存储
        workerExecutorEntityRepo.deleteByWorkerId(workerEntity.getWorkerId());
        if (CollectionUtils.isNotEmpty(options.getExecutors())) {
            List<WorkerExecutorEntity> workerExecutorEntities = options.getExecutors().stream()
                    .map(a -> WorkerConverter.toWorkerExecutorEntity(workerId, a))
                    .collect(Collectors.toList());
            workerExecutorEntityRepo.saveAll(workerExecutorEntities);
            workerExecutorEntityRepo.flush();
        }

        // Tags 存储
        workerTagEntityRepo.deleteByWorkerId(workerEntity.getWorkerId());
        if (CollectionUtils.isNotEmpty(options.getTags())) {
            List<WorkerTagEntity> workerTagEntities = options.getTags().stream()
                    .map(a -> WorkerConverter.toWorkerTagEntity(workerId, a))
                    .collect(Collectors.toList());
            workerTagEntityRepo.saveAll(workerTagEntities);
            workerTagEntityRepo.flush();
        }

        log.info("worker registered " + workerEntity.getWorkerId());

        return WorkerConverter.toRegisterDTO(workerEntity.getWorkerId(), nodeManger.allAlive());
    }

    /**
     * worker心跳
     *
     * @param option 心跳参数，上报部分指标数据
     */
    @Transactional(rollbackOn = Throwable.class)
    public WorkerRegisterDTO heartbeat(String workerId, WorkerHeartbeatParam option) {
        // 查询worker并校验
        WorkerEntity workerEntity = workerEntityRepo.findByWorkerIdAndDeleted(workerId, false).orElse(null);
        Verifies.requireNotNull(workerEntity, "worker不存在！");

        // Metric 存储
        WorkerMetricEntity workerMetricEntity = WorkerConverter.toWorkerMetricEntity(workerId, option.getAvailableResource());
        workerMetricEntityRepo.saveAndFlush(workerMetricEntity);

        if (log.isDebugEnabled()) {
            log.debug("receive heartbeat from " + workerId);
        }

        return WorkerConverter.toRegisterDTO(workerId, nodeManger.allAlive());
    }

    @Transactional
    public boolean updateStatus(String workerId, Integer oldStatus, Integer newStatus) {
        return workerEntityRepo.updateStatus(workerId, oldStatus, newStatus) > 0;
    }

    /**
     * 启动后参与调度
     *
     * @param workerId id
     */
    @Transactional
    public boolean start(String workerId) {
        Optional<WorkerEntity> workerEntityOptional = workerEntityRepo.findById(workerId);
        Verifies.verify(workerEntityOptional.isPresent(), String.format("Cannot find Worker %s", workerId));

        WorkerEntity workerEntity = workerEntityOptional.get();
        // 已经启动不重复处理
        if (workerEntity.isEnabled()) {
            return true;
        }

        return workerEntityRepo.updateEnable(workerEntity.getWorkerId(), false, true) == 1;
    }

    /**
     * 取消后不参与调度
     */
    @Transactional
    public boolean stop(String workerId) {
        // 获取当前的plan数据
        Optional<WorkerEntity> workerEntityOptional = workerEntityRepo.findById(workerId);
        Verifies.verify(workerEntityOptional.isPresent(), String.format("Cannot find Worker %s", workerId));

        // 已经停止不重复处理
        WorkerEntity workerEntity = workerEntityOptional.get();
        // 已经停止不重复处理
        if (!workerEntity.isEnabled()) {
            return true;
        }

        // 停用计划
        return workerEntityRepo.updateEnable(workerEntity.getWorkerId(), true, false) == 1;
    }

    public PageDTO<WorkerDTO> page(WorkerQueryParam param) {
        Specification<WorkerEntity> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            if (StringUtils.isNotBlank(param.getName())) {
                Predicate p3 = cb.like(root.get("name").as(String.class), param.getName() + "%");
                p.add(p3);
            }
            p.add(cb.equal(root.get("deleted"), false));
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

            List<String> workerIds = entities.stream().map(WorkerEntity::getWorkerId).collect(Collectors.toList());
            List<WorkerTagEntity> workerTagEntities = workerTagEntityRepo.findByWorkerIdIn(workerIds);
            Map<String, List<WorkerTagEntity>> workerTagMap = workerTagEntities.stream().collect(Collectors.groupingBy(WorkerTagEntity::getWorkerId));

            List<WorkerDTO> workerDTOS = new ArrayList<>();
            for (WorkerEntity entity : entities) {
                workerDTOS.add(WorkerConverter.toVO(entity, workerTagMap.get(entity.getWorkerId())));
            }

            page.setData(workerDTOS);
        }
        return page;
    }


}
