package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanRecord;
import org.limbo.flowjob.broker.core.plan.PlanRecordRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.converter.PlanRecordPoConverter;
import org.limbo.flowjob.broker.dao.mybatis.PlanRecordMapper;
import org.limbo.flowjob.broker.dao.po.PlanRecordPO;
import org.springframework.beans.factory.annotation.Autowired;
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
    public PlanRecord get(PlanRecord.ID planRecordId) {
        PlanRecordPO po = planRecordMapper.selectOne(Wrappers.<PlanRecordPO>lambdaQuery()
                .eq(PlanRecordPO::getPlanId, planRecordId.planId)
                .eq(PlanRecordPO::getPlanRecordId, planRecordId.planRecordId));
        return converter.reverse().convert(po);
    }


    @Override
    public PlanRecord.ID createId(String planId) {
        Long recentlyIdForUpdate = planRecordMapper.getRecentlyIdForUpdate(planId);
        long planRecordId = recentlyIdForUpdate == null ? 1L : recentlyIdForUpdate + 1;
        return new PlanRecord.ID(planId, planRecordId);
    }


    @Override
    public void end(PlanRecord.ID planRecordId, PlanScheduleStatus state) {
        planRecordMapper.update(null, Wrappers.<PlanRecordPO>lambdaUpdate()
                .set(PlanRecordPO::getState, state.status)
                .set(PlanRecordPO::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanRecordPO::getPlanId, planRecordId.planId)
                .eq(PlanRecordPO::getPlanRecordId, planRecordId.planRecordId)
                .eq(PlanRecordPO::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }

}