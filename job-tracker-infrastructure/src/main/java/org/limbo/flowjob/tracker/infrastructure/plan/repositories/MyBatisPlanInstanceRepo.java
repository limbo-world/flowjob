package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.dao.mybatis.PlanInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

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
    public void endInstance(String planId, Long planInstanceId, PlanScheduleStatus state) {
        planInstanceMapper.update(null, Wrappers.<PlanInstancePO>lambdaUpdate()
                .set(PlanInstancePO::getPlanId, planId)
                .set(PlanInstancePO::getState, state)
                .set(PlanInstancePO::getEndAt, LocalDateTime.now())
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId)
        );
    }

    @Override
    public PlanInstance getInstance(String planId, Long planInstanceId) {
        PlanInstancePO po = planInstanceMapper.selectOne(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId));
        return converter.reverse().convert(po);
    }

    @Override
    public Long createId(String planId) {
        Long recentlyIdForUpdate = planInstanceMapper.getRecentlyIdForUpdate(planId);
        return recentlyIdForUpdate == null ? 1L : recentlyIdForUpdate + 1;
    }

}
