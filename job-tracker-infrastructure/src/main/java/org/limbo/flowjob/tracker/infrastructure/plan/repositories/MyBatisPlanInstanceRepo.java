package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.dao.mybatis.PlanInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanInstanceRepo implements PlanInstanceRepository {

    @Autowired
    private PlanInstanceMapper planInstanceMapper;

    @Autowired
    private PlanInstancePoConverter converter;

    @Override
    public void add(PlanInstance instance) {
        PlanInstancePO po = converter.convert(instance);
        planInstanceMapper.insert(po);
    }

    @Override
    public void end(String planId, Long planRecordId, Integer planInstanceId, PlanScheduleStatus state) {
        planInstanceMapper.update(null, Wrappers.<PlanInstancePO>lambdaUpdate()
                .set(PlanInstancePO::getState, state.status)
                .set(PlanInstancePO::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanRecordId, planRecordId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId)
                .eq(PlanInstancePO::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }

    @Override
    public PlanInstance get(String planId, Long planRecordId, Integer planInstanceId) {
        PlanInstancePO po = planInstanceMapper.selectOne(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanRecordId, planRecordId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId)
        );
        return converter.reverse().convert(po);
    }

    @Override
    public List<PlanInstance> list(String planId, Long planRecordId) {
        List<PlanInstance> result = new ArrayList<>();
        List<PlanInstancePO> pos = planInstanceMapper.selectList(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planId)
                .eq(PlanInstancePO::getPlanRecordId, planRecordId)
        );
        if (CollectionUtils.isEmpty(pos)) {
            return result;
        }
        for (PlanInstancePO po : pos) {
            result.add(converter.reverse().convert(po));
        }
        return result;
    }

    @Override
    public Integer createId(String planId, Long planRecordId) {
        Integer recentlyIdForUpdate = planInstanceMapper.getRecentlyIdForUpdate(planId, planRecordId);
        return recentlyIdForUpdate == null ? 1 : recentlyIdForUpdate + 1;
    }

}
