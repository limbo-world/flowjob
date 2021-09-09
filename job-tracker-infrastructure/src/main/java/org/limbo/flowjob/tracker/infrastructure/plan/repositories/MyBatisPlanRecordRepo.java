package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;
import org.limbo.flowjob.tracker.dao.mybatis.PlanRecordMapper;
import org.limbo.flowjob.tracker.dao.po.PlanRecordPO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanRecordPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanRecordRepo implements PlanRecordRepository {

    @Autowired
    private PlanRecordMapper planRecordMapper;

    @Autowired
    private PlanRecordPoConverter converter;

    @Override
    public void add(PlanRecord record) {
        PlanRecordPO po = converter.convert(record);
        planRecordMapper.insert(po);
    }

    @Override
    public PlanRecord get(String planId, Long planRecordId) {
        PlanRecordPO po = planRecordMapper.selectOne(Wrappers.<PlanRecordPO>lambdaQuery()
                .eq(PlanRecordPO::getPlanId, planId)
                .eq(PlanRecordPO::getPlanRecordId, planRecordId));
        return converter.reverse().convert(po);
    }

    @Override
    public Long createId(String planId) {
        Long recentlyIdForUpdate = planRecordMapper.getRecentlyIdForUpdate(planId);
        return recentlyIdForUpdate == null ? 1L : recentlyIdForUpdate + 1;
    }

    @Override
    public void end(String planId, Long planRecordId, PlanScheduleStatus state) {
        planRecordMapper.update(null, Wrappers.<PlanRecordPO>lambdaUpdate()
                .set(PlanRecordPO::getState, state.status)
                .set(PlanRecordPO::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanRecordPO::getPlanId, planId)
                .eq(PlanRecordPO::getPlanRecordId, planRecordId)
                .eq(PlanRecordPO::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }
}
