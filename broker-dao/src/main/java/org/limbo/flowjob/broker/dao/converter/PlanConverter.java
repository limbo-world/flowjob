package org.limbo.flowjob.broker.dao.converter;

import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanInfoMapper;
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
public abstract class PlanConverter {

    @Setter(onMethod_ = @Inject)
    private ApplicationContext ac;

    @Setter(onMethod_ = @Inject)
    private PlanInfoMapper mapper;

    @Setter(onMethod_ = @Inject)
    private PlanInfoConverter planInfoConverter;


    public abstract PlanEntity toEntity(Plan plan);

    public abstract Plan toDO(PlanEntity entity);

    @AfterMapping
    public void afterMappingPlan(@MappingTarget Plan plan) {
        // 注入依赖
        ac.getAutowireCapableBeanFactory().autowireBean(plan);
        // 获取plan 的当前版本
        PlanInfoEntity po = mapper.selectById(plan.getCurrentVersion());
        PlanInfo currentVersion = planInfoConverter.toDO(po);
        plan.setInfo(currentVersion);
    }

}
