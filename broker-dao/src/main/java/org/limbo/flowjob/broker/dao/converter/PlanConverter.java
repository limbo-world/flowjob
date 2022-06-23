package org.limbo.flowjob.broker.dao.converter;

import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PlanConverter {

    @Setter(onMethod_ = @Inject)
    private ApplicationContext ac;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoConverter planInfoConverter;


    public abstract PlanEntity toEntity(Plan plan);

    public abstract Plan toDO(PlanEntity entity);

    @AfterMapping
    public void afterMappingPlan(@MappingTarget Plan plan) {
        // 注入依赖
        ac.getAutowireCapableBeanFactory().autowireBean(plan);
        // 获取plan 的当前版本
        Optional<PlanInfoEntity> planInfoEntityOptional = planInfoEntityRepo.findById(plan.getCurrentVersion());
        Verifies.verify(planInfoEntityOptional.isPresent(), "does not find info by version--" +plan.getCurrentVersion()+ "");
        PlanInfoEntity entity = planInfoEntityOptional.get();
        PlanInfo currentVersion = planInfoConverter.toDO(entity);
        plan.setInfo(currentVersion);
    }

}
