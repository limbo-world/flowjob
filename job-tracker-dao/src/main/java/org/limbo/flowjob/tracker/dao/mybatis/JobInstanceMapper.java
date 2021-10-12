package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.limbo.flowjob.tracker.dao.po.JobInstancePO;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface JobInstanceMapper extends BaseMapper<JobInstancePO> {

    @Select("select job_instance_id from flowjob_job_instance " +
            "where plan_id = #{planId} and plan_record_id = #{planRecordId} and plan_instance_id = #{planInstanceId} and job_id = #{jobId} " +
            "order by plan_instance_id desc limit 1 for update")
    Integer getRecentlyIdForUpdate(@Param("planId") String planId, @Param("planRecordId") Long planRecordId,
                                @Param("planInstanceId") Integer planInstanceId, @Param("jobId") String jobId);

}
