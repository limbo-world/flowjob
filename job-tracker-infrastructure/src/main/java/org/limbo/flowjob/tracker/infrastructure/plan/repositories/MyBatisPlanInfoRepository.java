package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.plan.PlanInfo;
import org.limbo.flowjob.tracker.core.plan.PlanInfoRepository;
import org.limbo.flowjob.broker.dao.mybatis.PlanInfoMapper;
import org.limbo.flowjob.broker.dao.po.PlanInfoPO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInfoPOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Repository
public class MyBatisPlanInfoRepository implements PlanInfoRepository {

    @Autowired
    private PlanInfoPOConverter converter;

    @Autowired
    private PlanInfoMapper mapper;


    /**
     * {@inheritDoc}
     * @param planInfo 执行计划版本信息
     */
    @Override
    public void addVersion(PlanInfo planInfo) {
        mapper.insert(converter.convert(planInfo));
    }

    /**
     * {@inheritDoc}
     * @param planId 执行计划ID
     * @param version 版本号
     * @return
     */
    @Override
    public PlanInfo getByVersion(String planId, Integer version) {
        PlanInfoPO po = mapper.selectOne(Wrappers.<PlanInfoPO>lambdaQuery()
                .eq(PlanInfoPO::getPlanId, planId)
                .eq(PlanInfoPO::getVersion, version));

        return converter.reverse().convert(po);
    }


}
