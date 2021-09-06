package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;
import org.limbo.flowjob.tracker.dao.mybatis.PlanRecordMapper;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class MyBatisPlanRecordRepo implements PlanRecordRepository {

    @Autowired
    private PlanRecordMapper planRecordMapper;

    @Autowired
    private PlanInstancePoConverter converter;

//    @Override
//    public void endInstance(String planId, Long planInstanceId, LocalDateTime endTime, PlanScheduleStatus state) {
//        planInstanceMapper.update(null, Wrappers.<PlanInstancePO>lambdaUpdate()
//                .set(PlanInstancePO::getPlanId, planId)
//                .set(PlanInstancePO::getState, state.status)
//                .set(PlanInstancePO::getEndAt, endTime)
//                .eq(PlanInstancePO::getPlanId, planId)
//                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId)
//        );
//    }


    @Override
    public void add(PlanRecord record) {
        PlanInstancePO po = converter.convert(instance);
        planInstanceMapper.insert(po);
    }

    @Override
    public PlanRecord get(String planId, Long planRecordId) {
        PlanInstancePO po = planInstanceMapper.selectOne(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId));
        return converter.reverse().convert(po);
    }

    @Override
    public Long createId(String planId) {
        Long recentlyIdForUpdate = planRecordMapper.getRecentlyIdForUpdate(planId);
        return recentlyIdForUpdate == null ? 1L : recentlyIdForUpdate + 1;
    }
}
