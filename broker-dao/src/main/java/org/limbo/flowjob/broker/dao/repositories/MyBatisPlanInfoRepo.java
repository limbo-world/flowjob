package org.limbo.flowjob.broker.dao.repositories;

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
     * @return
     */
    @Override
    public PlanInfo getByVersion(Long planInfoId) {
        PlanInfoEntity po = mapper.selectById(planInfoId);
        return converter.reverse().convert(po);
    }


}
