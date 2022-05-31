package org.limbo.flowjob.broker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.limbo.flowjob.broker.dao.po.PlanPO;

/**
 * @author Brozen
 * @since 2021-10-19
 */
public interface PlanMapper extends BaseMapper<PlanPO> {

//    /**
//     * 插入Plan记录，通过 insert on duplicate key update 语句实现。
//     * @param plan 需要新增的Plan
//     * @return 影响的记录条数
//     */
//    int insertOrUpdate(@Param("plan") PlanPO plan);

}
