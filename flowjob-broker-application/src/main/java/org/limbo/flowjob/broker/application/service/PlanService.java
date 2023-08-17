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
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.PlanDTO;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.PlanVersionDTO;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.PlanQueryParam;
import org.limbo.flowjob.api.param.console.PlanVersionParam;
import org.limbo.flowjob.api.param.console.ScheduleOptionParam;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.application.converter.PlanConverter;
import org.limbo.flowjob.broker.application.converter.factory.PlanFactory;
import org.limbo.flowjob.broker.application.support.JpaHelper;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
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
 * @author Brozen
 * @since 2022-06-11
 */
@Slf4j
@Service
public class PlanService {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    private BrokerSlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private PlanFactory factory;

    @Setter(onMethod_ = @Inject)
    private PlanConverter planConverter;

    @Transactional
    public String add(PlanParam.NormalPlanParam param) {
        JobInfo jobInfo = factory.createJob(param);
        return save(null, PlanType.STANDALONE, param, JacksonUtils.toJSONString(jobInfo));
    }

    @Transactional
    public String update(String planId, PlanParam.NormalPlanParam param) {
        JobInfo jobInfo = factory.createJob(param);
        return save(planId, PlanType.STANDALONE, param, JacksonUtils.toJSONString(jobInfo));
    }

    @Transactional
    public String save(String planId, PlanType planType, PlanParam param, String jobInfo) {
        PlanInfoEntity planInfoEntity = new PlanInfoEntity();
        planInfoEntity.setJobInfo(jobInfo);

        String planInfoId = idGenerator.generateId(IDType.PLAN_INFO);

        if (StringUtils.isBlank(planId)) {
            // create
            planId = idGenerator.generateId(IDType.PLAN);

            PlanEntity planEntity = new PlanEntity();
            planEntity.setCurrentVersion(planInfoId);
            planEntity.setRecentlyVersion(planInfoId);
            planEntity.setEnabled(false);
            planEntity.setPlanId(planId);
            planEntity.setName(param.getName());

            planEntity = planEntityRepo.saveAndFlush(planEntity);

            // 槽位保存
            PlanSlotEntity planSlotEntity = new PlanSlotEntity();
            planSlotEntity.setSlot(slotManager.slot(planEntity.getPlanId()));
            planSlotEntity.setPlanId(planEntity.getPlanId());
            planSlotEntityRepo.saveAndFlush(planSlotEntity);
        } else {
            // update
            PlanEntity planEntity = planEntityRepo.findById(planId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + planId));
            // 更新 Plan 版本信息
            int effected = planEntityRepo.updateVersion(planInfoId, planInfoId, param.getName(), planId, planEntity.getCurrentVersion(), planEntity.getRecentlyVersion());
            if (effected < 1) {
                throw new IllegalStateException("并发操作，更新Plan版本失败");
            }
        }

        // base info
        planInfoEntity.setPlanId(planId);
        planInfoEntity.setPlanInfoId(planInfoId);
        planInfoEntity.setPlanType(planType.type);
        planInfoEntity.setName(param.getName());
        planInfoEntity.setDescription(param.getDescription());
        planInfoEntity.setTriggerType(param.getTriggerType().type);
        // ScheduleOption
        ScheduleOptionParam scheduleOption = param.getScheduleOption();
        planInfoEntity.setScheduleType(scheduleOption.getScheduleType().type);
        planInfoEntity.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        planInfoEntity.setScheduleEndAt(scheduleOption.getScheduleEndAt());
        planInfoEntity.setScheduleDelay(scheduleOption.getScheduleDelay() == null ? 0L : scheduleOption.getScheduleDelay());
        planInfoEntity.setScheduleInterval(scheduleOption.getScheduleInterval() == null ? 0L : scheduleOption.getScheduleInterval());
        planInfoEntity.setScheduleCron(scheduleOption.getScheduleCron());
        planInfoEntity.setScheduleCronType(scheduleOption.getScheduleCronType());

        // 保存版本信息
        planInfoEntityRepo.saveAndFlush(planInfoEntity);

        return planId;
    }

    /**
     * 启动计划，开始调度
     *
     * @param planId 计划ID
     */
    @Transactional
    public boolean start(String planId) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        PlanEntity planEntity = planEntityOptional.get();
        // 已经启动不重复处理
        if (planEntity.isEnabled()) {
            return true;
        }

        return planEntityRepo.updateEnable(planEntity.getPlanId(), false, true) == 1;
    }

    /**
     * 取消计划 停止调度
     */
    @Transactional
    public boolean stop(String planId) {
        // 获取当前的plan数据
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        // 已经停止不重复处理
        PlanEntity planEntity = planEntityOptional.get();
        // 已经停止不重复处理
        if (!planEntity.isEnabled()) {
            return true;
        }

        // 停用计划
        return planEntityRepo.updateEnable(planEntity.getPlanId(), true, false) == 1;
    }

    public PlanInfoDTO.NormalPlanInfoDTO get(String planId) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        PlanEntity planEntity = planEntityOptional.get();
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planEntity.getCurrentVersion()).get();

        PlanInfoDTO.NormalPlanInfoDTO dto = new PlanInfoDTO.NormalPlanInfoDTO();
        dto.setPlanInfoId(planInfoEntity.getPlanInfoId());
        dto.setPlanId(planId);
        dto.setName(planInfoEntity.getName());
        dto.setDescription(planInfoEntity.getDescription());
        dto.setTriggerType(TriggerType.parse(planInfoEntity.getTriggerType()));
        dto.setScheduleOption(planConverter.toScheduleOptionDTO(planInfoEntity));

        JobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class);
        planConverter.assemble(dto, jobInfo);
        return dto;
    }

    public PageDTO<PlanDTO> page(PlanQueryParam param) {
        Specification<PlanEntity> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            if (StringUtils.isNotBlank(param.getName())) {
                Predicate p3 = cb.like(root.get("name").as(String.class), param.getName() + "%");
                p.add(p3);
            }
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("planId")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        Page<PlanEntity> queryResult = planEntityRepo.findAll(sf, pageable);
        List<PlanEntity> planEntities = queryResult.getContent();
        PageDTO<PlanDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(planEntities)) {
            List<PlanInfoEntity> planInfoEntities = planInfoEntityRepo.findAllById(planEntities.stream().map(PlanEntity::getCurrentVersion).collect(Collectors.toList()));
            Map<String, PlanInfoEntity> planInfoEntityMap = planInfoEntities.stream().collect(Collectors.toMap(PlanInfoEntity::getPlanInfoId, e -> e));
            page.setData(planEntities.stream().map(planEntity -> {
                PlanInfoEntity planInfoEntity = planInfoEntityMap.get(planEntity.getCurrentVersion());
                PlanDTO vo = new PlanDTO();
                vo.setPlanId(planEntity.getPlanId());
                vo.setCurrentVersion(planEntity.getCurrentVersion());
                vo.setRecentlyVersion(planEntity.getRecentlyVersion());
                vo.setEnabled(planEntity.isEnabled());
                vo.setName(planInfoEntity.getName());
                vo.setDescription(planInfoEntity.getDescription());
                vo.setPlanType(planInfoEntity.getPlanType());
                vo.setScheduleType(planInfoEntity.getScheduleType());
                vo.setTriggerType(planInfoEntity.getTriggerType());
                vo.setScheduleStartAt(planInfoEntity.getScheduleStartAt());
                vo.setScheduleEndAt(planInfoEntity.getScheduleEndAt());
                vo.setScheduleDelay(planInfoEntity.getScheduleDelay());
                vo.setScheduleInterval(planInfoEntity.getScheduleInterval());
                vo.setScheduleCron(planInfoEntity.getScheduleCron());
                vo.setScheduleCronType(planInfoEntity.getScheduleCronType());
                return vo;
            }).collect(Collectors.toList()));
        }
        return page;
    }

    public PageDTO<PlanVersionDTO> versionPage(PlanVersionParam param) {
        Specification<PlanInfoEntity> sf = (root, query, cb) -> {
            // 设置查询字段
            query.multiselect(root.get("planId"), root.get("planInfoId"), root.get("name"), root.get("createdAt"));
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            Predicate p3 = cb.like(root.get("planId").as(String.class), param.getPlanId());
            p.add(p3);
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("planInfoId")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        Page<PlanInfoEntity> queryResult = planInfoEntityRepo.findAll(sf, pageable);
        List<PlanInfoEntity> planInfoEntities = queryResult.getContent();
        PageDTO<PlanVersionDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        List<PlanVersionDTO> data = new ArrayList<>();
        page.setData(data);
        if (CollectionUtils.isEmpty(planInfoEntities)) {
            return page;
        }
        for (PlanInfoEntity planInfoEntity : planInfoEntities) {
            PlanVersionDTO dto = new PlanVersionDTO();
            dto.setPlanInfoId(planInfoEntity.getPlanInfoId());
            dto.setName(planInfoEntity.getName());
            dto.setCreatedAt(planInfoEntity.getCreatedAt());
            data.add(dto);
        }
        return page;
    }

    @Transactional
    public boolean versionUpdate(String planId, String version) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));
        Optional<PlanInfoEntity> planInfoEntityOptional = planInfoEntityRepo.findById(version);
        Verifies.verify(planInfoEntityOptional.isPresent(), String.format("Cannot find Version %s", version));
        PlanEntity planEntity = planEntityOptional.get();
        // 更新 Plan 版本信息
        int effected = planEntityRepo.updateVersion(version, planId, planEntity.getCurrentVersion());
        return effected > 0;
    }

}
