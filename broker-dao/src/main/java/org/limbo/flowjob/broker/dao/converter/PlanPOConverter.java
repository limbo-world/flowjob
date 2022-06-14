package org.limbo.flowjob.broker.dao.converter;

import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.repositories.PlanInfoRepository;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Mapper(componentModel="cdi")
public abstract class PlanPOConverter {

    @Setter(onMethod_ = @Inject)
    private ApplicationContext ac;

    @Setter(onMethod_ = @Inject)
    private PlanInfoRepository planInfoRepository;


    public abstract PlanEntity toEntity(Plan plan);

    public abstract Plan toDO(PlanEntity entity);

    @AfterMapping
    public void afterMappingPlan(@MappingTarget Plan plan) {
        // 注入依赖
        ac.getAutowireCapableBeanFactory().autowireBean(plan);
        // 获取plan 的当前版本
        PlanInfo currentVersion = planInfoRepository.getByVersion(plan.getCurrentVersion());
        plan.setInfo(currentVersion);
    }

}
