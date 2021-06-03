package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerPO;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface WorkerMapper extends BaseMapper<WorkerPO> {


    /**
     * 插入Worker记录，采用insert ignore语句
     * @param worker 工作节点
     * @return 影响的行数
     */
    int insertIgnore(WorkerPO worker);

}
