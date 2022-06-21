package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.converter.PlanRecordPoConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanInstanceRepo implements PlanInstanceRepository {

    @Autowired
    private PlanRecordMapper planRecordMapper;

    @Autowired
    private PlanRecordPoConverter converter;

    @Setter(onMethod_ = @Inject)
    private IDRepo idRepo;


    @Override
    @Transactional
    public String add(PlanInstance instance) {
        String planInstanceId = idRepo.createPlanInstanceId();

        instance.setPlanInstanceId(planInstanceId);

        PlanInstanceEntity po = converter.convert(instance);
        planRecordMapper.insert(po);

        return planInstanceId;
    }


    @Override
    public PlanInstance get(String planInstanceId) {
        PlanInstanceEntity po = planRecordMapper.selectOne(Wrappers.<PlanInstanceEntity>lambdaQuery()
                .eq(PlanInstanceEntity::getPlanInstanceId, planInstanceId)
        );
        return converter.reverse().convert(po);
    }


    @Override
    public void end(String planInstanceId, PlanScheduleStatus state) {
        planRecordMapper.update(null, Wrappers.<PlanInstanceEntity>lambdaUpdate()
                .set(PlanInstanceEntity::getState, state.status)
                .set(PlanInstanceEntity::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanInstanceEntity::getPlanInstanceId, planInstanceId)
                .eq(PlanInstanceEntity::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }

}
