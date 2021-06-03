package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerStatisticsPO;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface WorkerStatisticsMapper extends BaseMapper<WorkerStatisticsPO> {


    /**
     * 新增worker统计记录，使用insert ignore语句
     * @param statistics worker 统计记录
     * @return 影响的记录条数
     */
    int insertIgnore(WorkerStatisticsPO statistics);

}
