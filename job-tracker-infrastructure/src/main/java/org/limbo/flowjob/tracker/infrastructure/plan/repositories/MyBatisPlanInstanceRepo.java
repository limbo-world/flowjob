package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.dao.mybatis.PlanInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class MyBatisPlanInstanceRepo implements PlanInstanceRepository {

    @Autowired
    private PlanInstanceMapper planInstanceMapper;

    @Autowired
    private PlanInstancePoConverter converter;

    @Override
    public void addInstance(PlanInstance instance) {
        PlanInstancePO po = converter.convert(instance);
        planInstanceMapper.insert(po);
    }

    @Override
    public void updateInstance(PlanInstance instance) {
        planInstanceMapper.update(null, Wrappers.<PlanInstancePO>lambdaUpdate()
                .set(PlanInstancePO::getState, instance.getState())
                .eq(PlanInstancePO::getPlanId, instance.getPlanId())
                .eq(PlanInstancePO::getPlanInstanceId, instance.getPlanInstanceId())
        );
    }

    @Override
    public PlanInstance getInstance(String planId, String planInstanceId) {
        PlanInstancePO po = planInstanceMapper.selectOne(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId));
        return converter.reverse().convert(po);
    }

}
