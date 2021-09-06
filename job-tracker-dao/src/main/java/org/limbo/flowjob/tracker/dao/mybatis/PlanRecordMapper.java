package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.dao.po.PlanRecordPO;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanRecordMapper extends BaseMapper<PlanRecordPO> {

    @Select("select plan_record_id from flowjob_plan_record where plan_id = #{planId} order by plan_record_id desc limit 1 for update")
    Long getRecentlyIdForUpdate(@Param("planId") String planId);
}
