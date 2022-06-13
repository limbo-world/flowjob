package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.repositories.PlanInfoRepository;
import org.limbo.flowjob.broker.dao.converter.PlanInfoPOConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Repository
public class MyBatisPlanInfoRepo implements PlanInfoRepository {

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
        PlanInfoEntity po = mapper.selectOne(Wrappers.<PlanInfoEntity>lambdaQuery()
                .eq(PlanInfoEntity::getPlanInfoId, planId + version));
        return converter.reverse().convert(po);
    }


}
