package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.api.console.param.WorkflowJobParam;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.support.SlotManager;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Service
public class PlanService {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInfoEntityRepo jobInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    public Plan get(String id) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(id);
        Verifies.verify(planEntityOptional.isPresent(), "plan is not exist " + id);
        return domainConverter.toPlan(planEntityOptional.get());
    }

    @Transactional
    public String save(String planId, PlanParam param) {

        Verifies.verify(param.getPlanType() != null && PlanType.UNKNOWN != param.getPlanType(), MsgConstants.UNKNOWN + " Plan Type");
        if (PlanType.SINGLE == param.getPlanType()) {
            Verifies.notNull(param.getJob(), "Job can't be null with Single Plan Type");
        } else {
            Verifies.notEmpty(param.getWorkflow(), "Workflow can't be empty with Workflow Plan Type");
        }

        // 视图属性 todo 校验dag不为空 且内部节点不能重复 none 类型 检查有 api方式的最初节点 非此类型要检查有 schedule 的最初节点 感觉好像也没必要，用户自己保证

        String planInfoId = idGenerator.generateId(IDType.PLAN_INFO);

        if (StringUtils.isBlank(planId)) {
            planId = idGenerator.generateId(IDType.PLAN);

            PlanEntity planEntity = new PlanEntity();
            planEntity.setCurrentVersion(planInfoId);
            planEntity.setRecentlyVersion(planInfoId);
            planEntity.setEnabled(false);
            planEntity.setPlanId(planId);

            planEntity = planEntityRepo.saveAndFlush(planEntity);

            // 槽位保存
            PlanSlotEntity planSlotEntity = new PlanSlotEntity();
            planSlotEntity.setSlot(SlotManager.slot(planEntity.getPlanId()));
            planSlotEntity.setPlanId(planEntity.getPlanId());
            planSlotEntityRepo.saveAndFlush(planSlotEntity);
        } else {
            PlanEntity planEntity = planEntityRepo.findById(planId).orElse(null);

            Verifies.notNull(planEntity, "plan is null id:" + planId);

            // 更新 Plan 版本信息
            int effected = planEntityRepo.updateVersion(planInfoId, planInfoId, planId, planEntity.getCurrentVersion(), planEntity.getRecentlyVersion());
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
        planInfoEntity.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        planInfoEntity.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        planInfoEntity.setScheduleCron(scheduleOption.getScheduleCron());
        // job info
        if (PlanType.SINGLE == param.getPlanType()) {
            planInfoEntity.setJobInfo(JacksonUtils.toJSONString(param.getJob()));
        } else {
            // 保存jobInfo信息
            List<JobInfoEntity> jobInfoEntities = new ArrayList<>();
            for (WorkflowJobParam jobParam : param.getWorkflow()) {
                JobInfoEntity jobInfoEntity = new JobInfoEntity();
                jobInfoEntity.setPlanInfoId(planInfoId);
                jobInfoEntity.setName(jobParam.getName());
                jobInfoEntity.setType(jobParam.getType().type);
                jobInfoEntity.setTriggerType(jobParam.getTriggerType().type);
                jobInfoEntity.setAttributes(JacksonUtils.toJSONString(jobParam.getAttributes(), JacksonUtils.DEFAULT_NONE_OBJECT));
                jobInfoEntity.setDispatchOption(JacksonUtils.toJSONString(jobParam.getDispatchOption(), JacksonUtils.DEFAULT_NONE_OBJECT));
                jobInfoEntity.setExecutorName(jobParam.getExecutorName());
                jobInfoEntity.setTerminateWithFail(jobParam.isTerminateWithFail());

                jobInfoEntities.add(jobInfoEntity);
            }
            jobInfoEntityRepo.saveAll(jobInfoEntities);
            jobInfoEntityRepo.flush();
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
        if (planEntity.isEnabled()) {
            return true;
        }

        // 停用计划
        return planEntityRepo.updateEnable(planEntity.getPlanId(), true, false) == 1;
    }

}
