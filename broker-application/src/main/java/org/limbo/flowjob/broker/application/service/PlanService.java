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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.PageDTO;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.console.param.PlanQueryParam;
import org.limbo.flowjob.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.api.console.vo.PlanVO;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.converter.PlanConverter;
import org.limbo.flowjob.broker.application.support.JpaHelper;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.dag.DAG;
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
    private SlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private PlanConverter planConverter;

    @Transactional
    public String save(String planId, PlanParam param) {

        Verifies.verify(param.getPlanType() != null && PlanType.UNKNOWN != param.getPlanType(), MsgConstants.UNKNOWN + " Plan Type");
        if (PlanType.SINGLE == param.getPlanType()) {
            Verifies.notNull(param.getJob(), "Job can't be null with Single Plan Type");
        } else {
            Verifies.notEmpty(param.getWorkflow(), "Workflow can't be empty with Workflow Plan Type");
        }

        // 视图属性 todo v1 校验dag不为空 且内部节点不能重复 none 类型 检查有 api方式的最初节点 非此类型要检查有 schedule 的最初节点 感觉好像也没必要，用户自己保证

        String planInfoId = idGenerator.generateId(IDType.PLAN_INFO);

        if (StringUtils.isBlank(planId)) {
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
            PlanEntity planEntity = planEntityRepo.findById(planId).orElse(null);

            Verifies.notNull(planEntity, "plan is null id:" + planId);

            // 更新 Plan 版本信息
            int effected = planEntityRepo.updateVersion(planInfoId, planInfoId, param.getName(), planId, planEntity.getCurrentVersion(), planEntity.getRecentlyVersion());
            if (effected <= 0) {
                throw new IllegalStateException("更新Plan版本失败");
            }
        }

        PlanInfoEntity planInfoEntity = new PlanInfoEntity();
        // base info
        planInfoEntity.setPlanId(planId);
        planInfoEntity.setPlanInfoId(planInfoId);
        planInfoEntity.setPlanType(param.getPlanType().status);
        planInfoEntity.setName(param.getName());
        planInfoEntity.setDescription(param.getDescription());
        planInfoEntity.setTriggerType(param.getTriggerType().type);
        // ScheduleOption
        ScheduleOptionParam scheduleOption = param.getScheduleOption();
        planInfoEntity.setScheduleType(scheduleOption.getScheduleType().type);
        planInfoEntity.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        planInfoEntity.setScheduleDelay(scheduleOption.getScheduleDelay() == null ? 0L : scheduleOption.getScheduleDelay().toMillis());
        planInfoEntity.setScheduleInterval(scheduleOption.getScheduleInterval() == null ? 0L : scheduleOption.getScheduleInterval().toMillis());
        planInfoEntity.setScheduleCron(scheduleOption.getScheduleCron());
        // job info
        if (PlanType.SINGLE == param.getPlanType()) {
            JobInfo jobInfo = planConverter.covertJob(idGenerator.generateId(IDType.JOB_INFO), param.getJob());
            planInfoEntity.setJobInfo(JacksonUtils.toJSONString(jobInfo));
        } else {
            DAG<WorkflowJobInfo> workflow = planConverter.convertJob(param.getWorkflow());
            try {
                planInfoEntity.setJobInfo(workflow.json());
            } catch (JsonProcessingException e) {
                log.error("To DAG Json failed! dag={}", workflow, e);
                throw new VerifyException("Dag Node verify fail!");
            }
        }

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

    public PageDTO<PlanVO> page(PlanQueryParam param) {
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
        PageDTO<PlanVO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(planEntities)) {
            List<PlanInfoEntity> planInfoEntities = planInfoEntityRepo.findAllById(planEntities.stream().map(PlanEntity::getCurrentVersion).collect(Collectors.toList()));
            Map<String, PlanInfoEntity> planInfoEntityMap = planInfoEntities.stream().collect(Collectors.toMap(PlanInfoEntity::getPlanInfoId, e -> e));
            page.setData(planEntities.stream().map(planEntity -> {
                PlanInfoEntity planInfoEntity = planInfoEntityMap.get(planEntity.getCurrentVersion());
                PlanVO vo = new PlanVO();
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
                vo.setScheduleDelay(planInfoEntity.getScheduleDelay());
                vo.setScheduleInterval(planInfoEntity.getScheduleInterval());
                vo.setScheduleCron(planInfoEntity.getScheduleCron());
                vo.setScheduleCronType(planInfoEntity.getScheduleCronType());
                vo.setRetry(planInfoEntity.getRetry());
                return vo;
            }).collect(Collectors.toList()));
        }
        return page;
    }


}
