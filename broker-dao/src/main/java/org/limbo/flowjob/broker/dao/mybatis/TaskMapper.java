package org.limbo.flowjob.broker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface TaskMapper extends BaseMapper<TaskEntity> {

    @Select("select plan_instance_id from flowjob_plan_instance where plan_id = #{planId} and plan_record_id = #{planRecordId} order by plan_instance_id desc limit 1 for update")
    Long getRecentlyIdForUpdate(@Param("planId") String planId, @Param("planRecordId") Long planRecordId);
}
