package org.limbo.flowjob.broker.dao.converter;

import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Component
public class PlanConverter {

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoConverter planInfoConverter;

    public PlanEntity toEntity(Plan plan) {
        PlanEntity planEntity = new PlanEntity();
        planEntity.setCurrentVersion(Long.valueOf(plan.getCurrentVersion()));
        planEntity.setRecentlyVersion(Long.valueOf(plan.getRecentlyVersion()));
        planEntity.setIsEnabled(plan.isEnabled());
        planEntity.setId(Long.valueOf(plan.getPlanId()));
        return planEntity;
    }

    public Plan toDO(PlanEntity entity) {
        Plan plan = new Plan();
        plan.setPlanId(String.valueOf(entity.getId()));
        plan.setCurrentVersion(String.valueOf(entity.getCurrentVersion()));
        plan.setRecentlyVersion(String.valueOf(entity.getRecentlyVersion()));
        plan.setEnabled(entity.getIsEnabled());

        // 获取plan 的当前版本
        Optional<PlanInfoEntity> planInfoEntityOptional = planInfoEntityRepo.findById(Long.valueOf(plan.getCurrentVersion()));
        Verifies.verify(planInfoEntityOptional.isPresent(), "does not find info by version--" +plan.getCurrentVersion()+ "");
        PlanInfo currentVersion = planInfoConverter.toDO(planInfoEntityOptional.get());
        plan.setInfo(currentVersion);
        return plan;

    }

}
