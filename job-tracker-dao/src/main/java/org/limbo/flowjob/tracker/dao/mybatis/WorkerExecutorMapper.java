package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.limbo.flowjob.tracker.dao.po.WorkerExecutorPO;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-05
 */
public interface WorkerExecutorMapper extends BaseMapper<WorkerExecutorPO> {

    /**
     * 批量插入worker执行器
     * @param executors 执行器po列表
     */
    int batchInsert(@Param("executors") List<WorkerExecutorPO> executors);

    /**
     * 根据workerId查询所有执行器
     */
    @Select("SELECT * FROM worker_executor WHERE worker_id = #{workerId}")
    List<WorkerExecutorPO> findByWorker(@Param("workerId") String workerId);

}
